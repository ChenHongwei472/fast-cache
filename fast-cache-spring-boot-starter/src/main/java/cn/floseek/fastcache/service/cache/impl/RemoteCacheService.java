package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.cache.CacheService;
import cn.floseek.fastcache.service.redis.RedisService;

import java.util.function.Supplier;

/**
 * 分布式缓存服务实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class RemoteCacheService<K, V> implements CacheService<K, V> {

    protected final CacheConfig cacheConfig;
    protected final RedisService redisService;

    public RemoteCacheService(CacheConfig cacheConfig, RedisService redisService) {
        this.cacheConfig = cacheConfig;
        this.redisService = redisService;
    }

    @Override
    public V get(K key) {
        String cacheKey = this.buildRemoteCacheKey(key);
        return redisService.getObject(cacheKey);
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        V value = this.get(key);
        if (value != null) {
            return value;
        }

        value = dbLoader.get();
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        String cacheKey = this.buildRemoteCacheKey(key);
        if (cacheConfig.getExpireTime() == null) {
            redisService.setObject(cacheKey, value);
        } else {
            redisService.setObject(cacheKey, value, cacheConfig.getExpireTime());
        }
    }

    @Override
    public void remove(K key) {
        String cacheKey = this.buildRemoteCacheKey(key);
        redisService.deleteObject(cacheKey);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.REMOTE;
    }

    /**
     * 构建分布式缓存键
     *
     * @param key 缓存键
     * @return 分布式缓存键
     */
    private String buildRemoteCacheKey(K key) {
        return cacheConfig.getCacheName() + ":" + key.toString();
    }
}
