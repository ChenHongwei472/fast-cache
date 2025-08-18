package cn.floseek.fastcache.cache.builder;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCache;
import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存构建者
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public class MultiLevelCacheBuilder<K, V> implements CacheBuilder<K, V> {

    private final Cache<K, V> localCache;
    private final Cache<K, V> remoteCache;

    public MultiLevelCacheBuilder(Cache<K, V> localCache, Cache<K, V> remoteCache) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public Cache<K, V> build(CacheConfig config) {
        return new MultiLevelCache<>(config, this.localCache, this.remoteCache);
    }
}
