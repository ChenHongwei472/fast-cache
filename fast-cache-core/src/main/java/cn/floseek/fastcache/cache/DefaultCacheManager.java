package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.impl.LocalCache;
import cn.floseek.fastcache.cache.impl.MultiLevelCache;
import cn.floseek.fastcache.cache.impl.RemoteCache;
import cn.floseek.fastcache.broadcast.BroadcastManager;
import cn.floseek.fastcache.redis.RedisService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存管理类
 *
 * @author ChenHongwei472
 */
@Slf4j
public class DefaultCacheManager implements CacheManager {

    private final RedisService redisService;
    private final BroadcastManager broadcastManager;

    /**
     * 缓存映射
     */
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    public DefaultCacheManager(RedisService redisService, BroadcastManager broadcastManager) {
        this.redisService = redisService;
        this.broadcastManager = broadcastManager;
    }

    /**
     * 获取或创建缓存实例
     *
     * @param cacheConfig 缓存配置
     * @param <K>         缓存键类型
     * @param <V>         缓存值类型
     * @return 缓存实例
     */
    @Override
    public <K, V> Cache<K, V> getOrCreateCache(CacheConfig cacheConfig) {
        String cacheKey = cacheConfig.getCacheName() + "_" + cacheConfig.getCacheType();
        if (cacheMap.containsKey(cacheKey)) {
            return (Cache<K, V>) cacheMap.get(cacheKey);
        }

        Cache<K, V> cache = switch (cacheConfig.getCacheType()) {
            case LOCAL -> new LocalCache<>(cacheConfig, broadcastManager);
            case REMOTE -> new RemoteCache<>(cacheConfig, redisService);
            case MULTI_LEVEL -> new MultiLevelCache<>(cacheConfig, redisService, broadcastManager);
        };
        broadcastManager.startSubscribe();
        cacheMap.put(cacheKey, cache);
        return cache;
    }

    @Override
    public void close() throws Exception {
        try {
            broadcastManager.close();
        } catch (Exception e) {
            log.error("关闭广播服务失败", e);
        }

        cacheMap.clear();
    }
}
