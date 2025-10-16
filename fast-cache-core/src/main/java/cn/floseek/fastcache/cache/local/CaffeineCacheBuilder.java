package cn.floseek.fastcache.cache.local;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.common.enums.LocalCacheProvider;

/**
 * Caffeine 缓存构建器
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class CaffeineCacheBuilder<K, V> extends LocalCacheBuilder<K, V> {

    @Override
    public Cache<K, V> build(CacheConfig<K, V> config) {
        return new CaffeineCache<>(config);
    }

    @Override
    public LocalCacheProvider getProvider() {
        return LocalCacheProvider.CAFFEINE;
    }

}
