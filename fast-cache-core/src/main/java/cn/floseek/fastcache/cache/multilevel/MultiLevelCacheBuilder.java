package cn.floseek.fastcache.cache.multilevel;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.builder.CacheBuilder;
import cn.floseek.fastcache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存构建器实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public class MultiLevelCacheBuilder<K, V> implements CacheBuilder<K, V> {

    /**
     * 本地缓存
     */
    private final Cache<K, V> localCache;
    /**
     * 分布式缓存
     */
    private final Cache<K, V> remoteCache;

    public MultiLevelCacheBuilder(Cache<K, V> localCache, Cache<K, V> remoteCache) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public Cache<K, V> build(CacheConfig<K, V> config) {
        return new MultiLevelCache<>(config, this.localCache, this.remoteCache);
    }

}
