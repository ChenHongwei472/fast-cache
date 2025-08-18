package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.builder.CacheBuilderManager;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.cache.builder.MultiLevelCacheBuilder;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.decorator.BroadcastDecorator;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.config.GlobalProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存管理类
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
        if (cacheBuilderManager == null) {
            throw new IllegalStateException("缓存构建器模板未初始化");
        }

        String cacheName = config.getCacheName();
        if (cacheName == null || cacheName.isEmpty()) {
            throw new IllegalArgumentException("缓存名称不能为空");
        }

        String cacheKey = this.buildMapKey(config.getCacheType(), cacheName);
        if (cacheMap.containsKey(cacheKey)) {
            return (Cache<K, V>) cacheMap.get(cacheKey);
        }

        Cache<K, V> cache;
        if (config.getCacheType() == CacheType.LOCAL) {
            cache = new BroadcastDecorator<>(this.buildLocalCache(config));
        } else if (config.getCacheType() == CacheType.REMOTE) {
            cache = this.buildRemoteCache(config);
        } else {
            Cache<K, V> localCache = this.buildLocalCache(config);
            Cache<K, V> remoteCache = this.buildRemoteCache(config);

            MultiLevelCacheBuilder<K, V> cacheBuilder = new MultiLevelCacheBuilder<>(localCache, remoteCache);
            cache = new BroadcastDecorator<>(cacheBuilder.build(config));
        }
        cacheMap.put(cacheKey, cache);

        this.buildBroadcastManager(config);

        return cache;
    }

    @Override
    public <K, V> Cache<K, V> getCache(CacheType cacheType, String cacheName) {
        String cacheKey = this.buildMapKey(cacheType, cacheName);
        return (Cache<K, V>) cacheMap.get(cacheKey);
    }

    @Override
    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    @Override
    public void close() throws Exception {
        if (broadcastManager != null) {
            try {
                broadcastManager.close();
            } catch (Exception e) {
                log.error("关闭广播管理器失败", e);
            }
        }

        cacheMap.clear();
    }

    /**
     * 构建映射的 key
     *
     * @param cacheType 缓存类型
     * @param cacheName 缓存名称
     * @return 映射的 key
     */
    private String buildMapKey(CacheType cacheType, String cacheName) {
        return cacheName + "_" + cacheType;
    }

    /**
     * 创建本地缓存
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 本地缓存
     */
    private <K, V> Cache<K, V> buildLocalCache(CacheConfig config) {
        LocalCacheBuilder<K, V> cacheBuilder = (LocalCacheBuilder<K, V>) cacheBuilderManager.getLocalCacheBuilder(globalProperties.getLocal().getProvider());
        if (cacheBuilder == null) {
            throw new IllegalArgumentException("本地缓存构建器不存在");
        }

        return cacheBuilder.build(config);
    }

    /**
     * 创建分布式缓存
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 分布式缓存
     */
    private <K, V> Cache<K, V> buildRemoteCache(CacheConfig config) {
        RemoteCacheBuilder<K, V> cacheBuilder = (RemoteCacheBuilder<K, V>) cacheBuilderManager.getRemoteCacheBuilder(globalProperties.getRemote().getProvider());
        if (cacheBuilder == null) {
            throw new IllegalArgumentException("分布式缓存构建器不存在");
        }

        return cacheBuilder.build(config);
    }

    /**
     * 创建广播管理器
     *
     * @param config 缓存配置
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     */
    private <K, V> void buildBroadcastManager(CacheConfig config) {
        log.info("开始创建广播管理器...");
        if (!config.isSyncLocalCache()) {
            log.info("当前已关闭同步本地缓存，将不创建广播管理器");
            config.setBroadcastManager(null);
            return;
        }

        if (config.getBroadcastManager() == null) {
            log.info("当前未配置广播管理器，将重新创建广播管理器");
            RemoteCacheBuilder<K, V> cacheBuilder = (RemoteCacheBuilder<K, V>) cacheBuilderManager.getRemoteCacheBuilder(globalProperties.getRemote().getProvider());
            if (cacheBuilder == null) {
                throw new IllegalArgumentException("分布式缓存构建者不存在");
            }

            if (!cacheBuilder.supportBroadcast()) {
                log.info("当前分布式缓存构建者不支持创建广播");
                return;
            }

            broadcastManager = cacheBuilder.createBroadcastManager(this);
            config.setBroadcastManager(broadcastManager);
            log.info("创建广播管理器成功");
        } else {
            log.info("当前已配置广播管理器，将使用已配置的广播管理器");
            broadcastManager = config.getBroadcastManager();
        }

        // 启动订阅
        broadcastManager.startSubscribe();
        log.info("已成功启动广播管理器订阅");
    }
}
