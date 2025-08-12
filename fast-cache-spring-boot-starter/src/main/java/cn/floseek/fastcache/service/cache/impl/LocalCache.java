package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import cn.floseek.fastcache.service.cache.Cache;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 本地缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class LocalCache<K, V> implements Cache<K, V> {

    private final CacheConfig cacheConfig;
    private final BroadcastService broadcastService;

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    public LocalCache(CacheConfig cacheConfig, BroadcastService broadcastService) {
        this.cacheConfig = cacheConfig;
        this.broadcastService = broadcastService;

        this.cache = LocalCacheManager.getInstance().getOrCreateCache(cacheConfig.getCacheName(), cacheConfig);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        V value = cache.getIfPresent(key);
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
        return cache.getAllPresent(keys);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        broadcastService.broadcast(cacheConfig.getCacheName(), key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
        broadcastService.broadcast(cacheConfig.getCacheName(), map.keySet());
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
        broadcastService.broadcast(cacheConfig.getCacheName(), key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        cache.invalidateAll(keys);
        broadcastService.broadcast(cacheConfig.getCacheName(), keys);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.LOCAL;
    }
}
