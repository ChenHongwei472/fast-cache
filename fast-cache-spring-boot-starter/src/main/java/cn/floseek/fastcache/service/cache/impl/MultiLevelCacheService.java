package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import cn.floseek.fastcache.service.cache.CacheService;
import cn.floseek.fastcache.service.redis.RedisService;

import java.util.function.Supplier;

/**
 * 多级缓存服务实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class MultiLevelCacheService<K, V> implements CacheService<K, V> {

    private final CacheConfig cacheConfig;
    private final LocalCacheService<K, V> localCacheService;
    private final RemoteCacheService<K, V> remoteCacheService;
    private final BroadcastService broadcastService;

    public MultiLevelCacheService(CacheConfig cacheConfig, LocalCacheManager localManager, RedisService redisService, BroadcastService broadcastService) {
        this.cacheConfig = cacheConfig;
        this.localCacheService = new LocalCacheService<>(cacheConfig, localManager);
        this.remoteCacheService = new RemoteCacheService<>(cacheConfig, redisService);
        this.broadcastService = broadcastService;
    }

    @Override
    public V get(K key) {
        V value = localCacheService.get(key);
        if (value != null) {
            return value;
        }

        value = remoteCacheService.get(key);
        if (value != null) {
            localCacheService.put(key, value);
        }
        return value;
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        V value = this.get(key);
        if (value != null) {
            return value;
        }

        value = dbLoader.get();
        if (value != null) {
            this.put(key, value);
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        remoteCacheService.put(key, value);
        localCacheService.put(key, value);
        broadcastService.broadcast(cacheConfig.getCacheName(), key);
    }

    @Override
    public void remove(K key) {
        remoteCacheService.remove(key);
        localCacheService.remove(key);
        broadcastService.broadcast(cacheConfig.getCacheName(), key);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MULTI_LEVEL;
    }
}
