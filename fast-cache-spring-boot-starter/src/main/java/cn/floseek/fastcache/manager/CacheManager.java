package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import cn.floseek.fastcache.service.cache.Cache;
import cn.floseek.fastcache.service.cache.impl.LocalCache;
import cn.floseek.fastcache.service.cache.impl.MultiLevelCache;
import cn.floseek.fastcache.service.cache.impl.RemoteCache;
import cn.floseek.fastcache.service.redis.RedisService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理类
 *
 * @author ChenHongwei472
 */
public class CacheManager {

    private final RedisService redisService;
    private final BroadcastService broadcastService;

    /**
     * 缓存映射
     */
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    public CacheManager(RedisService redisService, BroadcastService broadcastService) {
        this.redisService = redisService;
        this.broadcastService = broadcastService;
    }

    /**
     * 获取或创建缓存实例
     *
     * @param cacheConfig 缓存配置
     * @param <K>         缓存键类型
     * @param <V>         缓存值类型
     * @return 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getOrCreateCache(CacheConfig cacheConfig) {
        String cacheKey = cacheConfig.getCacheName() + "_" + cacheConfig.getCacheType();
        if (cacheMap.containsKey(cacheKey)) {
            return (Cache<K, V>) cacheMap.get(cacheKey);
        }

        Cache<K, V> cache = switch (cacheConfig.getCacheType()) {
            case LOCAL -> new LocalCache<>(cacheConfig, broadcastService);
            case REMOTE -> new RemoteCache<>(cacheConfig, redisService);
            case MULTI_LEVEL -> new MultiLevelCache<>(cacheConfig, redisService, broadcastService);
        };
        cacheMap.put(cacheKey, cache);
        return cache;
    }
}
