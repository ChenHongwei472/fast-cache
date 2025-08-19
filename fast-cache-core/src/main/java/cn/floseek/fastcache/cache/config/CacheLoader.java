package cn.floseek.fastcache.cache.config;

import java.util.function.Function;

/**
 * 缓存加载器
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@FunctionalInterface
public interface CacheLoader<K, V> extends Function<K, V> {

    /**
     * 加载缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    V load(K key);

    @Override
    default V apply(K key) {
        return this.load(key);
    }
}
