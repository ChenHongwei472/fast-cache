package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.builder.CacheBuilderManager;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.common.enums.LocalCacheProvider;
import cn.floseek.fastcache.common.enums.RemoteCacheProvider;
import cn.floseek.fastcache.common.enums.SyncStrategy;
import cn.floseek.fastcache.cache.decorator.BroadcastDecorator;
import cn.floseek.fastcache.cache.decorator.CacheLoaderDecorator;
import cn.floseek.fastcache.cache.decorator.RefreshCacheDecorator;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCacheBuilder;
import cn.floseek.fastcache.common.exception.CacheException;
import cn.floseek.fastcache.config.GlobalProperties;
import cn.floseek.fastcache.lock.LockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存管理实现
 *
 * @author ChenHongwei472
 */
@Slf4j
@SuppressWarnings("unchecked")
public class DefaultCacheManager implements CacheManager {

    /**
     * 缓存映射
     */
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private GlobalProperties globalProperties;
    private CacheBuilderManager<?, ?> cacheBuilderManager;
    private LockTemplate lockTemplate;
    private BroadcastManager broadcastManager;

    private DefaultCacheManager() {
    }

    public DefaultCacheManager(GlobalProperties globalProperties, CacheBuilderManager<?, ?> cacheBuilderManager, LockTemplate lockTemplate) {
        this.globalProperties = globalProperties;
        this.cacheBuilderManager = cacheBuilderManager;
        this.lockTemplate = lockTemplate;

        // 初始化广播管理器
        this.initBroadcastManager();
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(CacheConfig<K, V> config) {
        // 参数校验
        if (cacheBuilderManager == null) {
            throw new CacheException("CacheBuilderManager is not initialized");
        }
        String cacheName = config.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            throw new CacheException("Cache name must not be empty");
        }

        // 初始化配置参数
        if (Objects.isNull(config.getSyncStrategy())) {
            config.syncStrategy(globalProperties.getSyncStrategy());
        }
        if (Objects.isNull(config.getLocalMaximumSize())) {
            config.localMaximumSize(globalProperties.getLocal().getMaximumSize());
        }
        if (Objects.isNull(config.getKeyConverter())) {
            config.keyConverter(globalProperties.getRemote().getKeyConverter().getKeyConverter());
        }
        if (Objects.isNull(config.getSerializer())) {
            config.serializer(globalProperties.getRemote().getSerializer().getSerializer());
        }

        // 生成映射的 key
        String cacheMapKey = this.generateMapKey(config.getCacheType(), cacheName);

        // 使用双重检查锁定模式获取缓存
        Cache<K, V> cache = (Cache<K, V>) cacheMap.get(cacheMapKey);
        if (cache == null) {
            synchronized (cacheMap) {
                cache = (Cache<K, V>) cacheMap.get(cacheMapKey);
                if (cache == null) {
                    cache = this.createCache(config);
                    cacheMap.put(cacheMapKey, cache);
                }
            }
        }

        // 订阅广播频道
        if (config.isSyncEnabled() && Objects.nonNull(broadcastManager) && !broadcastManager.isSubscribed()) {
            this.subscribeBroadcast(config.getSyncStrategy());
        }
        return cache;
    }

    @Override
    public <K, V> Cache<K, V> getCache(CacheType cacheType, String cacheName) {
        String cacheMapKey = this.generateMapKey(cacheType, cacheName);
        return (Cache<K, V>) cacheMap.get(cacheMapKey);
    }

    @Override
    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(broadcastManager)) {
            try {
                broadcastManager.close();
            } catch (Exception e) {
                log.error("Closing broadcast manager failed", e);
            }
            broadcastManager = null;
        }

        if (MapUtils.isNotEmpty(cacheMap)) {
            cacheMap.forEach((key, cache) -> {
                try {
                    cache.close();
                } catch (Exception e) {
                    log.error("Closing cache failed, key: {}", key, e);
                }
            });
            cacheMap.clear();
        }
    }

    /**
     * 生成映射的 key
     *
     * @param cacheType 缓存类型
     * @param cacheName 缓存名称
     * @return 映射的 key
     */
    private String generateMapKey(CacheType cacheType, String cacheName) {
        return cacheName + "_" + cacheType;
    }

    /**
     * 创建缓存实例
     *
     * @param config 缓存配置对象
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 缓存实例
     */
    private <K, V> Cache<K, V> createCache(CacheConfig<K, V> config) {
        // 创建缓存实例
        Cache<K, V> cache;
        if (config.getCacheType() == CacheType.LOCAL) {
            cache = this.createLocalCache(config);
        } else if (config.getCacheType() == CacheType.REMOTE) {
            cache = this.createRemoteCache(config);
        } else {
            Cache<K, V> localCache = this.createLocalCache(config);
            Cache<K, V> remoteCache = this.createRemoteCache(config);
            MultiLevelCacheBuilder<K, V> builder = new MultiLevelCacheBuilder<>(localCache, remoteCache);

            cache = builder.build(config);
        }

        if (Objects.nonNull(config.getLoader())) {
            if (Objects.nonNull(config.getRefreshPolicy())) {
                // 添加缓存刷新装饰器
                cache = new RefreshCacheDecorator<>(cache, lockTemplate);
            } else {
                // 添加缓存加载器装饰器
                cache = new CacheLoaderDecorator<>(cache);
            }
        }

        // 添加广播装饰器
        if (config.isSyncEnabled()) {
            return new BroadcastDecorator<>(cache, broadcastManager);
        }

        return cache;
    }

    /**
     * 创建本地缓存
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 本地缓存
     */
    private <K, V> Cache<K, V> createLocalCache(CacheConfig<K, V> config) {
        LocalCacheProvider provider = globalProperties.getLocal().getProvider();
        LocalCacheBuilder<K, V> builder = (LocalCacheBuilder<K, V>) cacheBuilderManager.getLocalCacheBuilder(provider);

        if (builder == null) {
            throw new CacheException("LocalCacheBuilder not found for provider: " + provider);
        }

        return builder.build(config);
    }

    /**
     * 创建分布式缓存
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 分布式缓存
     */
    private <K, V> Cache<K, V> createRemoteCache(CacheConfig<K, V> config) {
        RemoteCacheProvider provider = globalProperties.getRemote().getProvider();
        RemoteCacheBuilder<K, V> builder = (RemoteCacheBuilder<K, V>) cacheBuilderManager.getRemoteCacheBuilder(provider);

        if (builder == null) {
            throw new CacheException("Remote cache builder not found for provider: " + provider);
        }

        return builder.build(config);
    }

    /**
     * 初始化广播管理器
     */
    private void initBroadcastManager() {
        log.info("Initializing and subscribing broadcast manager");
        RemoteCacheProvider provider = globalProperties.getRemote().getProvider();
        RemoteCacheBuilder<?, ?> builder = cacheBuilderManager.getRemoteCacheBuilder(provider);

        if (builder == null) {
            log.warn("Remote cache builder not found for provider: {}", provider);
            return;
        }

        if (!builder.supportBroadcast()) {
            log.debug("Broadcast not supported by provider: {}", provider);
            return;
        }

        broadcastManager = builder.createBroadcastManager(this);
        if (globalProperties.getSyncStrategy() == SyncStrategy.NONE) {
            log.info("Broadcast manager initialized, skip subscribe broadcast");
            return;
        }

        broadcastManager.subscribe();
        log.info("Broadcast manager initialized and subscribed");
    }

    /**
     * 订阅广播频道
     *
     * @param syncStrategy 缓存同步策略
     */
    private void subscribeBroadcast(SyncStrategy syncStrategy) {
        log.info("Subscribing broadcast manager");
        if (syncStrategy == SyncStrategy.NONE) {
            log.debug("Sync strategy is NONE, skip subscribe broadcast");
            return;
        }
        if (broadcastManager == null) {
            log.debug("Broadcast manager not initialized, skip subscribe broadcast");
            return;
        }
        broadcastManager.subscribe();
        log.info("Broadcast manager subscribed");
    }

}
