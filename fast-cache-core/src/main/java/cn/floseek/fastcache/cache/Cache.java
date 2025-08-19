package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;

import java.util.Collection;
import java.util.Map;

/**
 * 缓存接口
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public interface Cache<K, V> {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值
     */
    V get(K key);

    /**
     * 批量获取缓存值
     *
     * @param keys 缓存键集合
     * @return 缓存值映射
     */
    Map<K, V> getAll(Collection<? extends K> keys);

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void put(K key, V value);

    /**
     * 批量设置缓存值
     *
     * @param map 映射
     */
    void putAll(Map<? extends K, ? extends V> map);

    /**
     * 删除缓存值
     *
     * @param key 缓存键
     */
    void remove(K key);

    /**
     * 批量删除缓存值
     *
     * @param keys 缓存键集合
     */
    void removeAll(Collection<? extends K> keys);

    /**
     * 获取缓存类型
     *
     * @return 缓存类型
     */
    CacheType getCacheType();

    /**
     * 获取缓存配置
     *
     * @return 缓存配置对象
     */
    CacheConfig getConfig();
}
