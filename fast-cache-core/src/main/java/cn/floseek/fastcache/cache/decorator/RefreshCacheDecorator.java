package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.AbstractLocalCache;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheLoader;
import cn.floseek.fastcache.cache.config.RefreshPolicy;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCache;
import cn.floseek.fastcache.lock.LockTemplate;
import cn.floseek.fastcache.util.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存刷新装饰器
 * <p>
 * 提供了自动刷新缓存的能力，主要作用是防止缓存失效时造成的缓存雪崩，支持分布式环境下的刷新锁机制
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public class RefreshCacheDecorator<K, V> extends CacheLoaderDecorator<K, V> {

    /**
     * 缓存刷新线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "fast-cache-refresh-";
    /**
     * 缓存刷新锁键
     */
    private static final String REFRESH_LOCK_KEY = "refresh_lock";
    /**
     * 缓存刷新时间戳键
     */
    private static final String REFRESH_TIMESTAMP_KEY = "refresh_timestamp";

    /**
     * 缓存刷新任务映射
     */
    private final ConcurrentHashMap<Object, RefreshTask> refreshTaskMap = new ConcurrentHashMap<>();
    /**
     * 缓存刷新调度器
     */
    private static final ScheduledThreadPoolExecutor SCHEDULER;
    /**
     * 缓存刷新任务计数器
     */
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);

    static {
        log.info("Initializing cache refresh scheduler");
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME_PREFIX + THREAD_COUNT.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };

        SCHEDULER = new ScheduledThreadPoolExecutor(
                10, threadFactory, new ThreadPoolExecutor.DiscardPolicy());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down cache refresh scheduler");
            SCHEDULER.shutdownNow();
        }));
    }

    public RefreshCacheDecorator(Cache<K, V> decoratedCache) {
        super(decoratedCache);
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (Objects.nonNull(value)) {
            this.addOrUpdateRefreshTask(key);
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Map<K, V> valueMap = super.getAll(keys);
        if (MapUtils.isNotEmpty(valueMap)) {
            valueMap.keySet().forEach(this::addOrUpdateRefreshTask);
        }
        return valueMap;
    }

    /**
     * 停止刷新缓存
     */
    protected void stopRefresh() {
        refreshTaskMap.values().forEach(RefreshTask::cancel);
        refreshTaskMap.clear();
    }

    /**
     * 添加或更新缓存刷新任务
     *
     * @param key 缓存键
     */
    protected void addOrUpdateRefreshTask(K key) {
        RefreshPolicy refreshPolicy = config.getRefreshPolicy();
        if (Objects.isNull(refreshPolicy)) {
            return;
        }

        long refreshMillis = refreshPolicy.getRefreshMillis();
        if (refreshMillis <= 0) {
            return;
        }

        RefreshTask refreshTask = refreshTaskMap.computeIfAbsent(key, obj -> {
            log.debug("Adding cache refresh task for key: {}, interval: {}ms", key, refreshMillis);
            RefreshTask task = new RefreshTask(key);
            task.lastAccessTime = System.currentTimeMillis();
            task.future = SCHEDULER.scheduleWithFixedDelay(task, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS);
            return task;
        });

        refreshTask.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * 缓存刷新任务
     */
    class RefreshTask implements Runnable {

        private final K key;
        private long lastAccessTime;
        private ScheduledFuture<?> future;

        public RefreshTask(K key) {
            this.key = key;
        }

        @Override
        public void run() {
            RefreshPolicy refreshPolicy = config.getRefreshPolicy();
            if (Objects.isNull(refreshPolicy) || !config.loaderEnabled()) {
                cancel();
                return;
            }

            long currentTime = System.currentTimeMillis();
            long stopRefreshAfterLastAccessMillis = refreshPolicy.getStopRefreshAfterLastAccessMillis();

            if (stopRefreshAfterLastAccessMillis > 0 && (lastAccessTime + stopRefreshAfterLastAccessMillis < currentTime)) {
                cancel();
                return;
            }

            log.debug("Refreshing cache for key: {}", key);
            Cache<K, V> cache = unwrapAll();

            if (cache instanceof AbstractLocalCache<K, V>) {
                this.refreshCache(cache);
            } else {
                this.refreshRemoteOrMultiLevelCache(cache, currentTime);
            }
        }

        /**
         * 取消缓存刷新任务
         */
        private void cancel() {
            log.debug("Canceling cache refresh task for key: {}", key);
            if (Objects.nonNull(future)) {
                future.cancel(false);
            }
            refreshTaskMap.remove(key);
        }

        /**
         * 刷新缓存
         *
         * @param cache 缓存实例
         */
        private void refreshCache(Cache<K, V> cache) {
            CacheLoader<K, V> loader = config.getLoader();
            if (Objects.isNull(loader)) {
                return;
            }

            try {
                V value = loader.load(key);
                if (Objects.nonNull(value)) {
                    cache.put(key, value);
                    log.trace("Refreshed cache value for key: {}", key);
                }
            } catch (Exception e) {
                log.warn("Failed to refresh cache for key: {}", key, e);
            }
        }

        /**
         * 刷新分布式或多级缓存
         *
         * @param cache       缓存实例
         * @param currentTime 当前时间
         */
        @SuppressWarnings("unchecked")
        private void refreshRemoteOrMultiLevelCache(Cache<K, V> cache, long currentTime) {
            // 获取本地缓存和分布式缓存
            Cache<K, V> localCache = null;
            Cache<K, V> remoteCache;
            if (cache instanceof MultiLevelCache<K, V> multiLevelCache) {
                localCache = multiLevelCache.getLocalCache();
                remoteCache = multiLevelCache.getRemoteCache();
            } else {
                remoteCache = cache;
            }

            // 获取刷新策略
            RefreshPolicy refreshPolicy = config.getRefreshPolicy();
            long refreshMillis = refreshPolicy.getRefreshMillis();
            long refreshLockTimeoutMillis = refreshPolicy.getRefreshLockTimeoutMillis();

            // 检查时间戳确定是否需要刷新
            String timestampKey = CacheUtils.generateKey(REFRESH_TIMESTAMP_KEY, key.toString());
            Cache<String, Long> timestampCache = (Cache<String, Long>) remoteCache;
            Long lastRefreshTime = timestampCache.get(timestampKey);
            boolean shouldRefresh = Objects.isNull(lastRefreshTime) || (currentTime >= lastRefreshTime + refreshMillis);

            log.debug("Should refresh for key {}: {}", key, shouldRefresh);

            if (!shouldRefresh) {
                // 如果本地缓存不为空，则从分布式缓存中同步数据到本地缓存中
                if (Objects.nonNull(localCache)) {
                    V value = remoteCache.get(key);
                    localCache.put(key, value);
                    log.debug("Synchronized from remote to local cache, key: {}", key);
                }
                return;
            }

            // 获取分布式锁模板
            LockTemplate lockTemplate = config.getLockTemplate();
            if (Objects.isNull(lockTemplate)) {
                log.debug("Lock template not available, skipping refresh for key: {}", key);
                return;
            }

            String lockKey = CacheUtils.generateKey(REFRESH_LOCK_KEY, key.toString());
            try {
                boolean locked = lockTemplate.tryLock(lockKey, 0, refreshLockTimeoutMillis, TimeUnit.MILLISECONDS);
                if (!locked) {
                    log.debug("Refresh skipped, another instance is refreshing key: {}", key);
                    return;
                }

                this.refreshCache(cache);
                timestampCache.put(timestampKey, System.currentTimeMillis());
            } catch (InterruptedException e) {
                log.error("Refresh task interrupted for key: {}", key, e);
                Thread.currentThread().interrupt();
            } finally {
                lockTemplate.unlock(lockKey);
            }
        }
    }
}
