package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.builder.CacheBuilderManager;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.cache.config.LocalCacheProvider;
import cn.floseek.fastcache.cache.config.RemoteCacheProvider;
import cn.floseek.fastcache.cache.decorator.BroadcastDecorator;
import cn.floseek.fastcache.cache.decorator.CacheLoaderDecorator;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCacheBuilder;
import cn.floseek.fastcache.config.GlobalProperties;
import lombok.extern.slf4j.Slf4j;
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

    private final GlobalProperties globalProperties;
    private final CacheBuilderManager<?, ?> cacheBuilderManager;

    /**
     * 缓存映射
     */
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 广播管理器
     */
    private BroadcastManager broadcastManager;

    public DefaultCacheManager(GlobalProperties globalProperties, CacheBuilderManager<?, ?> cacheBuilderManager) {
        this.globalProperties = globalProperties;
        this.cacheBuilderManager = cacheBuilderManager;
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(CacheConfig config) {
        // 参数校验
        if (cacheBuilderManager == null) {
            throw new IllegalStateException("CacheBuilderManager is not initialized");
        }
        String cacheName = config.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            throw new IllegalArgumentException("Cache name must not be empty");
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

        // 初始化广播管理器
        this.initBroadcastManager(config);
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
                log.error("关闭广播管理器失败", e);
            }
        }

        cacheMap.clear();
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
    private <K, V> Cache<K, V> createCache(CacheConfig config) {
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

        // 添加缓存加载器装饰器
        if (Objects.nonNull(config.getLoader())) {
            cache = new CacheLoaderDecorator<>(cache);
        }

        // 添加广播装饰器
        return new BroadcastDecorator<>(cache);
    }

    /**
     * 创建本地缓存
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 本地缓存
     */
    private <K, V> Cache<K, V> createLocalCache(CacheConfig config) {
        LocalCacheProvider provider = globalProperties.getLocal().getProvider();
        LocalCacheBuilder<K, V> builder = (LocalCacheBuilder<K, V>) cacheBuilderManager.getLocalCacheBuilder(provider);

        if (builder == null) {
            throw new IllegalArgumentException("LocalCacheBuilder not found for provider: " + provider);
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
    private <K, V> Cache<K, V> createRemoteCache(CacheConfig config) {
        RemoteCacheProvider provider = globalProperties.getRemote().getProvider();
        RemoteCacheBuilder<K, V> builder = (RemoteCacheBuilder<K, V>) cacheBuilderManager.getRemoteCacheBuilder(provider);

        if (builder == null) {
            throw new IllegalArgumentException("Remote cache builder not found for provider: " + provider);
        }

        return builder.build(config);
    }

    /**
     * 初始化广播管理器
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     */
    private <K, V> void initBroadcastManager(CacheConfig config) {
        // 检查是否启用缓存同步
        if (!config.isSyncCache()) {
            log.debug("Cache sync is disabled, skip init BroadcastManager");
            config.setBroadcastManager(null);
            return;
        }

        // 检查是否配置广播管理器
        if (config.getBroadcastManager() == null) {
            log.info("BroadcastManager not configured, starting to create BroadcastManager");
            RemoteCacheProvider provider = globalProperties.getRemote().getProvider();
            RemoteCacheBuilder<K, V> builder = (RemoteCacheBuilder<K, V>) cacheBuilderManager.getRemoteCacheBuilder(provider);
            if (builder == null) {
                log.warn("Remote cache builder not available, skip init BroadcastManager");
                return;
            }

            if (!builder.supportBroadcast()) {
                log.debug("RemoteCacheBuilder not support broadcast, init BroadcastManager");
                return;
            }

            broadcastManager = builder.createBroadcastManager(this);
            config.setBroadcastManager(broadcastManager);
            log.info("BroadcastManager has been successfully created");
        } else {
            log.info("BroadcastManager configured, using existing instance");
            broadcastManager = config.getBroadcastManager();
        }

        // 订阅消息
        broadcastManager.subscribe();
        log.info("Initialized and subscribed BroadcastManager");
    }
}
