package cn.floseek.fastcache.cache.local;

import cn.floseek.fastcache.cache.AbstractLocalCache;
import cn.floseek.fastcache.config.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.time.DurationUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Caffeine 缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class CaffeineCache<K, V> extends AbstractLocalCache<K, V> {

    private final Cache<K, V> cache;

    public CaffeineCache(CacheConfig<K, V> config) {
        super(config);

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (Objects.nonNull(config.getLocalMaximumSize())) {
            caffeine.maximumSize(config.getLocalMaximumSize());
        }
        if (Objects.nonNull(config.getLocalExpireTime()) && DurationUtils.isPositive(config.getLocalExpireTime())) {
            caffeine.expireAfterWrite(config.getLocalExpireTime());
        }
        this.cache = caffeine.build();
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
