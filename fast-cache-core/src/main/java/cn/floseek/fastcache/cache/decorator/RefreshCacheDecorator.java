package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.AbstractRemoteCache;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheLoader;
import cn.floseek.fastcache.cache.config.RefreshPolicy;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCache;
import cn.floseek.fastcache.lock.LockTemplate;
import cn.floseek.fastcache.util.CacheUtils;
import lombok.extern.slf4j.Slf4j;

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
 * 提供了自动刷新缓存的能力，主要作用是防止缓存失效时造成的缓存雪崩
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public class RefreshCacheDecorator<K, V> extends CacheLoaderDecorator<K, V> {

    private final ConcurrentHashMap<Object, RefreshTask> taskMap = new ConcurrentHashMap<>();

    private static final ScheduledThreadPoolExecutor SCHEDULER;

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);

    private final CacheConfig<K, V> config;

    static {
        log.info("Init cache refresh scheduler");
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "fast-cache-refresh-" + THREAD_COUNT.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        SCHEDULER = new ScheduledThreadPoolExecutor(
                10, threadFactory, new ThreadPoolExecutor.DiscardPolicy());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown cache refresh scheduler");
            SCHEDULER.shutdownNow();
        }));
    }

    public RefreshCacheDecorator(Cache<K, V> decoratedCache) {
        super(decoratedCache);
        this.config = decoratedCache.getConfig();
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (Objects.nonNull(value)) {
            this.addOrUpdateRefreshTask(key, config.getLoader());
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Map<K, V> valueMap = super.getAll(keys);
        if (Objects.nonNull(valueMap)) {
            valueMap.keySet().forEach(key -> this.addOrUpdateRefreshTask(key, config.getLoader()));
        }
        return valueMap;
    }

    /**
     * 停止刷新缓存
     */
    protected void stopRefresh() {
        taskMap.values().forEach(RefreshTask::cancel);
    }

    /**
     * 添加或更新缓存刷新任务
     *
     * @param key    缓存键
     * @param loader 缓存加载器
     */
    protected void addOrUpdateRefreshTask(K key, CacheLoader<K, V> loader) {
        RefreshPolicy refreshPolicy = config.getRefreshPolicy();
        if (Objects.isNull(refreshPolicy)) {
            return;
        }

        long refreshMillis = refreshPolicy.getRefreshMillis();
        if (refreshMillis > 0) {
            RefreshTask refreshTask = taskMap.computeIfAbsent(key, obj -> {
                log.debug("Add refresh cache task, key: {}, interval: {}", key, refreshMillis);
                RefreshTask task = new RefreshTask(key, loader);
                task.lastAccessTime = System.currentTimeMillis();
                task.future = SCHEDULER.scheduleWithFixedDelay(task, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS);
                return task;
            });
            refreshTask.lastAccessTime = System.currentTimeMillis();
        }
    }

    /**
     * 缓存刷新任务
     */
    class RefreshTask implements Runnable {

        private final K key;
        private final CacheLoader<K, V> loader;

        private long lastAccessTime;
        private ScheduledFuture<?> future;

        public RefreshTask(K key, CacheLoader<K, V> loader) {
            this.key = key;
            this.loader = loader;
        }

        /**
         * 取消缓存刷新任务
         */
        private void cancel() {
            log.debug("Cancel refresh task, key: {}", key);
            if (Objects.nonNull(future)) {
                future.cancel(false);
            }
            taskMap.remove(key);
        }

        /**
         * 刷新缓存
         *
         * @param cache 缓存实例
         */
        private void refresh(Cache<K, V> cache) {
            if (Objects.nonNull(loader)) {
                try {
                    V value = loader.load(key);
                    if (Objects.nonNull(value)) {
                        cache.put(key, value);
                    }
                } catch (Exception e) {
                    log.warn("Refresh task failed for key: {}", key, e);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            if (config.getRefreshPolicy() == null || !config.loaderEnabled()) {
                cancel();
                return;
            }

            long now = System.currentTimeMillis();
            long refreshMillis = config.getRefreshPolicy().getRefreshMillis();
            long stopRefreshAfterLastAccessMillis = config.getRefreshPolicy().getStopRefreshAfterLastAccessMillis();
            if (stopRefreshAfterLastAccessMillis > 0) {
                if (lastAccessTime + stopRefreshAfterLastAccessMillis < now) {
                    cancel();
                    return;
                }
            }

            log.debug("Refresh key: {}", key);
            Cache<K, V> cache = unwrapAll();
            if (cache instanceof AbstractRemoteCache<K, V> || cache instanceof MultiLevelCache<K, V>) {
                String timestampKey = CacheUtils.generateKey("refresh-lock-timestamp", key);

                Cache<String, Long> timestampCache = (Cache<String, Long>) (cache instanceof MultiLevelCache<K, V> multiLevelCache
                        ? multiLevelCache.getRemoteCache()
                        : cache);
                Long timestamp = timestampCache.get(timestampKey);

                boolean shouldRefresh;
                if (Objects.nonNull(timestamp)) {
                    shouldRefresh = now >= timestamp + refreshMillis;
                } else {
                    shouldRefresh = true;
                }

                log.debug("Should refresh cache: {}", shouldRefresh);

                if (!shouldRefresh) {
                    if (cache instanceof MultiLevelCache<K, V> multiLevelCache) {
                        Cache<K, V> remoteCache = multiLevelCache.getRemoteCache();
                        Cache<K, V> localCache = multiLevelCache.getLocalCache();
                        V value = remoteCache.get(key);
                        localCache.put(key, value);
                        log.debug("Refresh cache from remote cache to local cache, key: {}", key);
                    }
                    return;
                }

                LockTemplate lockTemplate = config.getLockTemplate();
                if (Objects.isNull(lockTemplate)) {
                    log.debug("No LockTemplate available, skip refresh cache, key: {}", key);
                    return;
                }

                String lockKey = CacheUtils.generateKey("refresh-lock", key);
                try {
                    long leaseMillis = config.getRefreshPolicy().getRefreshLockTimeoutMillis();
                    boolean locked = lockTemplate.tryLock(lockKey, 0, leaseMillis, TimeUnit.MILLISECONDS);

                    if (!locked) {
                        log.debug("Refresh cache failed, perhaps another thread is refreshing, key: {}", key);
                        return;
                    }

                    this.refresh(cache);
                    timestampCache.put(timestampKey, System.currentTimeMillis());
                } catch (InterruptedException e) {
                    log.error("Refresh cache task interrupted, key: {}", key, e);
                } finally {
                    lockTemplate.unlock(lockKey);
                }
            } else {
                this.refresh(cache);
            }
        }
    }
}
