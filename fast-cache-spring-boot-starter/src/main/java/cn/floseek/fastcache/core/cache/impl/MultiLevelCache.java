package cn.floseek.fastcache.core.cache.impl;

import cn.floseek.fastcache.core.cache.Cache;
import cn.floseek.fastcache.manager.broadcast.BroadcastManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.RedisService;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 多级缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class MultiLevelCache<K, V> implements Cache<K, V> {

    private final LocalCache<K, V> localCacheService;
    private final RemoteCache<K, V> remoteCacheService;

    public MultiLevelCache(CacheConfig cacheConfig, RedisService redisService, BroadcastManager broadcastManager) {
        this.localCacheService = new LocalCache<>(cacheConfig, broadcastManager);
        this.remoteCacheService = new RemoteCache<>(cacheConfig, redisService);
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
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Map<K, V> valueMap = localCacheService.getAll(keys);
        if (valueMap != null && !valueMap.isEmpty()) {
            return valueMap;
        }

        valueMap = remoteCacheService.getAll(keys);
        if (valueMap != null && !valueMap.isEmpty()) {
            localCacheService.putAll(valueMap);
        }
        return valueMap;
    }

    @Override
    public void put(K key, V value) {
        remoteCacheService.put(key, value);
        localCacheService.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        remoteCacheService.putAll(map);
        localCacheService.putAll(map);
    }

    @Override
    public void remove(K key) {
        remoteCacheService.remove(key);
        localCacheService.remove(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        remoteCacheService.removeAll(keys);
        localCacheService.removeAll(keys);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MULTI_LEVEL;
    }
}
