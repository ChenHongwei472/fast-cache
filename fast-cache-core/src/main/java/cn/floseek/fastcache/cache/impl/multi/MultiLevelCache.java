package cn.floseek.fastcache.cache.impl.multi;

import cn.floseek.fastcache.cache.AbstractCache;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

/**
 * 多级缓存
 *
 * @author ChenHongwei472
 */
@Getter
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {

    private final Cache<K, V> localCache;
    private final Cache<K, V> remoteCache;

    public MultiLevelCache(CacheConfig config, Cache<K, V> localCache, Cache<K, V> remoteCache) {
        super(config);
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public V get(K key) {
        V value = localCache.get(key);
        if (value != null) {
            return value;
        }

        value = remoteCache.get(key);
        if (value != null) {
            localCache.put(key, value);
            return value;
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Map<K, V> valueMap = localCache.getAll(keys);
        if (valueMap != null && !valueMap.isEmpty()) {
            return valueMap;
        }

        valueMap = remoteCache.getAll(keys);
        if (valueMap != null && !valueMap.isEmpty()) {
            localCache.putAll(valueMap);
        }
        return valueMap;
    }

    @Override
    public void put(K key, V value) {
        remoteCache.put(key, value);
        localCache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        remoteCache.putAll(map);
        localCache.putAll(map);
    }

    @Override
    public void remove(K key) {
        remoteCache.remove(key);
        localCache.remove(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        remoteCache.removeAll(keys);
        localCache.removeAll(keys);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MULTI_LEVEL;
    }
}
