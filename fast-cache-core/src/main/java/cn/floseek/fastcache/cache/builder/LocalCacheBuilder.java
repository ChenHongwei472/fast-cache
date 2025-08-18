package cn.floseek.fastcache.cache.builder;

import cn.floseek.fastcache.cache.config.LocalCacheProvider;

/**
 * 本地缓存构建器抽象类
 * <p>
 * 定义本地缓存构建器的公共方法
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public abstract class LocalCacheBuilder<K, V> implements CacheBuilder<K, V> {

    /**
     * 获取本地缓存提供者
     *
     * @return 本地缓存提供者枚举
     */
    public abstract LocalCacheProvider getProvider();
}
