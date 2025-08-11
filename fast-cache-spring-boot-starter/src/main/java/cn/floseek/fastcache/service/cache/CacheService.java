package cn.floseek.fastcache.service.cache;

import cn.floseek.fastcache.model.CacheType;

import java.util.function.Supplier;

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
     * 获取缓存值
     *
     * @param key      缓存键
     * @param dbLoader 数据库加载器
     * @return 缓存值
     */
    V get(K key, Supplier<V> dbLoader);

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void put(K key, V value);

    /**
     * 删除缓存值
     *
     * @param key 缓存键
     */
    void remove(K key);

    /**
     * 获取缓存类型
     *
     * @return 缓存类型
     */
    CacheType getCacheType();
}
