package cn.floseek.fastcache.cache.builder;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;

/**
 * 缓存构建者接口
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public interface CacheBuilder<K, V> {

    /**
     * 构建缓存
     *
     * @param config 缓存配置
     * @return 缓存
     */
    Cache<K, V> build(CacheConfig config);
}
