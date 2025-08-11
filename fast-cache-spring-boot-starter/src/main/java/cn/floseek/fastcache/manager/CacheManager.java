package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import cn.floseek.fastcache.service.cache.CacheService;
import cn.floseek.fastcache.service.cache.impl.LocalCacheService;
import cn.floseek.fastcache.service.cache.impl.MultiLevelCacheService;
import cn.floseek.fastcache.service.cache.impl.RemoteCacheService;
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
    private final LocalCacheManager localCacheManager;
    private final BroadcastService broadcastService;

    /**
     * 缓存服务映射
     */
    private final Map<String, CacheService<?, ?>> cacheServiceMap = new ConcurrentHashMap<>();

    public CacheManager(RedisService redisService, LocalCacheManager localCacheManager, BroadcastService broadcastService) {
        this.redisService = redisService;
        this.localCacheManager = localCacheManager;
        this.broadcastService = broadcastService;
    }

    /**
     * 获取或创建缓存服务
     *
     * @param cacheConfig 缓存配置
     * @param <K>         缓存键类型
     * @param <V>         缓存值类型
     * @return 缓存服务
     */
    @SuppressWarnings("unchecked")
    public <K, V> CacheService<K, V> getOrCreateCacheService(CacheConfig cacheConfig) {
        String cacheKey = cacheConfig.getCacheName() + "_" + cacheConfig.getCacheType();
        if (cacheServiceMap.containsKey(cacheKey)) {
            return (CacheService<K, V>) cacheServiceMap.get(cacheKey);
        }

        CacheService<K, V> cacheService = switch (cacheConfig.getCacheType()) {
            case LOCAL -> new LocalCacheService<>(cacheConfig, localCacheManager);
            case REMOTE -> new RemoteCacheService<>(cacheConfig, redisService);
            case MULTI_LEVEL ->
                    new MultiLevelCacheService<>(cacheConfig, localCacheManager, redisService, broadcastService);
        };
        cacheServiceMap.put(cacheKey, cacheService);
        return cacheService;
    }
}
