package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.cache.Cache;
import cn.floseek.fastcache.service.redis.RedisService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 分布式缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class RemoteCache<K, V> implements Cache<K, V> {

    protected final CacheConfig cacheConfig;
    protected final RedisService redisService;

    public RemoteCache(CacheConfig cacheConfig, RedisService redisService) {
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
        String cacheKey = this.buildRemoteCacheKey(key);
        V value = redisService.getObject(cacheKey);
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
    public Map<K, V> getAll(Collection<? extends K> keys) {
        List<K> keyList = new ArrayList<>(keys);

        List<String> cacheKeys = keyList.stream()
                .map(this::buildRemoteCacheKey)
                .toList();
        List<V> objectList = redisService.getObjects(cacheKeys);

        Map<K, V> valueMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            V value = objectList.get(i);
            if (value != null) {
                valueMap.put(key, value);
            }
        }
        return valueMap;
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
    public void putAll(Map<? extends K, ? extends V> map) {
        Map<String, V> remoteCacheMap = new HashMap<>();
        map.forEach((key, value) -> remoteCacheMap.put(this.buildRemoteCacheKey(key), value));

        if (cacheConfig.getExpireTime() == null) {
            redisService.setObjects(remoteCacheMap);
        } else {
            redisService.setObjects(remoteCacheMap, cacheConfig.getExpireTime());
        }
    }

    @Override
    public void remove(K key) {
        String cacheKey = this.buildRemoteCacheKey(key);
        redisService.deleteObject(cacheKey);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        List<String> cacheKeys = keys.stream()
                .map(this::buildRemoteCacheKey)
                .toList();
        redisService.deleteObjects(cacheKeys);
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
