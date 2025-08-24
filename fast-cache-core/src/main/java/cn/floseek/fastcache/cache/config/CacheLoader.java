package cn.floseek.fastcache.cache.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 缓存加载器
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    /**
     * 加载缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    V load(K key);

    /**
     * 批量加载缓存
     *
     * @param keys 缓存键集合
     * @return 缓存值映射
     */
    default Map<K, V> loadAll(Collection<K> keys) {
        Map<K, V> resultMap = new HashMap<>(keys.size());
        for (K key : keys) {
            V value = this.load(key);
            if (Objects.nonNull(value)) {
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }
}
