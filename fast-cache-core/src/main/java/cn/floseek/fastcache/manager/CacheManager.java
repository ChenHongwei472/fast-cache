package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.core.cache.Cache;
import cn.floseek.fastcache.model.CacheConfig;

/**
 * 缓存管理器接口
 *
 * @author ChenHongwei472
 */
public interface CacheManager extends AutoCloseable {

    /**
     * 获取或创建缓存实例
     *
     * @param cacheConfig 缓存配置
     * @param <K>         缓存键类型
     * @param <V>         缓存值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getOrCreateCache(CacheConfig cacheConfig);
}
