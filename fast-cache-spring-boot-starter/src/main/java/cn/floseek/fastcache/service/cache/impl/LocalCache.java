package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.cache.Cache;

import java.util.function.Supplier;

/**
 * 本地缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class LocalCache<K, V> implements Cache<K, V> {

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    public LocalCache(CacheConfig cacheConfig) {
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
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.LOCAL;
    }
}
