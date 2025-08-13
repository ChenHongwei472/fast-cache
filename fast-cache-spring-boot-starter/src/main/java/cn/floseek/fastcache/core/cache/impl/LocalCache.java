package cn.floseek.fastcache.core.cache.impl;

import cn.floseek.fastcache.core.cache.Cache;
import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.manager.broadcast.BroadcastManager;
import cn.floseek.fastcache.model.CacheConfig;
import cn.floseek.fastcache.model.CacheMessage;
import cn.floseek.fastcache.model.CacheType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private final BroadcastManager broadcastManager;

    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;

    public LocalCache(CacheConfig cacheConfig, BroadcastManager broadcastManager) {
        this.cacheConfig = cacheConfig;
        this.broadcastManager = broadcastManager;

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
        this.broadcast(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
        this.broadcast(map.keySet());
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
        this.broadcast(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        cache.invalidateAll(keys);
        this.broadcast(keys);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.LOCAL;
    }

    /**
     * 缓存广播
     *
     * @param key 缓存键
     */
    private void broadcast(K key) {
        if (!cacheConfig.isSyncLocalCache()) {
            return;
        }

        CacheMessage cacheMessage = CacheMessage.builder()
                .instanceId(broadcastManager.getInstanceId())
                .cacheName(cacheConfig.getCacheName())
                .keys(List.of(key))
                .build();
        broadcastManager.publish(cacheMessage);
    }

    /**
     * 缓存广播
     *
     * @param keys 缓存键
     */
    private void broadcast(Collection<? extends K> keys) {
        if (!cacheConfig.isSyncLocalCache()) {
            return;
        }

        CacheMessage cacheMessage = CacheMessage.builder()
                .instanceId(broadcastManager.getInstanceId())
                .cacheName(cacheConfig.getCacheName())
                .keys(new ArrayList<>(keys))
                .build();
        broadcastManager.publish(cacheMessage);
    }
}
