package cn.floseek.fastcache.service.cache.impl;

import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheType;
import cn.floseek.fastcache.service.cache.CacheService;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.function.Supplier;

/**
 * 本地缓存服务实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class LocalCacheService<K, V> implements CacheService<K, V> {

    private final Cache<K, V> cache;

    public LocalCacheService(CacheConfig cacheConfig, LocalCacheManager localCacheManager) {
        this.cache = localCacheManager.getOrCreateCache(cacheConfig.getCacheName(), cacheConfig);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        return cache.get(key, k -> dbLoader.get());
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
