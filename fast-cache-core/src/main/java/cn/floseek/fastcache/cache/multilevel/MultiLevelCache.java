package cn.floseek.fastcache.cache.multilevel;

import cn.floseek.fastcache.cache.AbstractCache;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.config.CacheConfig;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 多级缓存实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Getter
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {

    /**
     * 本地缓存
     */
    private final Cache<K, V> localCache;

    /**
     * 分布式缓存
     */
    private final Cache<K, V> remoteCache;

    public MultiLevelCache(CacheConfig<K, V> config, Cache<K, V> localCache, Cache<K, V> remoteCache) {
        super(config);
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public V get(K key) {
        // 首先尝试从本地缓存中获取数据
        V value = localCache.get(key);
        if (Objects.nonNull(value)) {
            return value;
        }

        // 缓存未命中，则从分布式缓存中获取数据
        value = remoteCache.get(key);
        if (Objects.nonNull(value)) {
            localCache.put(key, value);
            return value;
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        // 缓存结果
        Map<K, V> resultMap = new HashMap<>(keys.size());
        // 未命中的键
        Set<K> missingKeys = new HashSet<>(keys);

        // 首先尝试从本地缓存中获取数据
        Map<K, V> valueMap = localCache.getAll(keys);

        // 如果获取到数据，则添加到缓存结果中，并移除未命中的键
        if (MapUtils.isNotEmpty(valueMap)) {
            resultMap.putAll(valueMap);
            missingKeys.removeAll(valueMap.keySet());
        }

        // 如果有未命中的键，则尝试从分布式缓存中获取数据
        if (CollectionUtils.isNotEmpty(missingKeys)) {
            valueMap = remoteCache.getAll(missingKeys);
            // 如果获取到数据，则添加到缓存结果中，并回填到本地缓存中
            if (MapUtils.isNotEmpty(valueMap)) {
                resultMap.putAll(valueMap);
                localCache.putAll(valueMap);
            }
        }

        return resultMap;
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
