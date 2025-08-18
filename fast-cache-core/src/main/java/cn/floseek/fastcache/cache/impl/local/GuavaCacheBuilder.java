package cn.floseek.fastcache.cache.impl.local;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.cache.config.LocalCacheProvider;

/**
 * Guava 缓存构建者
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class GuavaCacheBuilder<K, V> extends LocalCacheBuilder<K, V> {

    @Override
    public Cache<K, V> build(CacheConfig config) {
        return new GuavaCache<>(config);
    }

    @Override
    public LocalCacheProvider getProvider() {
        return LocalCacheProvider.GUAVA;
    }
}
