package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.config.RemoteCacheProvider;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import org.redisson.api.RedissonClient;

/**
 * Redisson 缓存构建者
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class RedissonCacheBuilder<K, V> extends RemoteCacheBuilder<K, V> {

    private final RedissonClient redissonClient;

    public RedissonCacheBuilder(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        return new RedissonBroadcastManager(cacheManager, redissonClient);
    }

    @Override
    public RemoteCacheProvider getProvider() {
        return RemoteCacheProvider.REDISSON;
    }

    @Override
    public Cache<K, V> build(CacheConfig config) {
        return new RedissonCache<>(config, redissonClient);
    }
}
