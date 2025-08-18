package cn.floseek.fastcache.cache.impl.local;

import cn.floseek.fastcache.cache.AbstractLocalCache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * Guava 缓存
 *
 * @author ChenHongwei472
 */
public class GuavaCache<K, V> extends AbstractLocalCache<K, V> {

    private final Cache<K, V> cache;

    public GuavaCache(CacheConfig config) {
        super(config);

        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(config.getMaximumSize());
        if (config.getExpireAfterWrite() != null) {
            cacheBuilder.expireAfterWrite(config.getExpireAfterWrite());
        }
        if (config.getExpireAfterAccess() != null) {
            cacheBuilder.expireAfterAccess(config.getExpireAfterAccess());
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
