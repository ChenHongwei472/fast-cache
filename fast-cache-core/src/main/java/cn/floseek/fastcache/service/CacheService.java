package cn.floseek.fastcache.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存服务接口
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public interface CacheService<K, V> {

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
     * 批量获取缓存值
     *
     * @param keys 缓存键集合
     * @return 缓存值列表
     */
    List<V> listAll(Collection<? extends K> keys);

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
     * 刷新缓存值
     *
     * @param key 缓存键
     * @return 缓存值
     */
    V refresh(K key);

}
