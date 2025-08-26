package cn.floseek.fastcache.cache.impl.local;

import cn.floseek.fastcache.cache.AbstractLocalCache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Guava 缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class GuavaCache<K, V> extends AbstractLocalCache<K, V> {

    private final Cache<K, V> cache;

    public GuavaCache(CacheConfig<K, V> config) {
        super(config);

        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if (Objects.nonNull(config.getLocalMaximumSize())) {
            cacheBuilder.maximumSize(config.getLocalMaximumSize());
        }
        if (Objects.nonNull(config.getLocalExpireTime())) {
            cacheBuilder.expireAfterWrite(config.getLocalExpireTime());
        }
        this.cache = cacheBuilder.build();
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        return cache.getAllPresent(keys);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        cache.invalidateAll(keys);
    }
}
