package cn.floseek.fastcache.cache.builder;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.config.RemoteCacheProvider;

/**
 * 分布式缓存构建者
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public abstract class RemoteCacheBuilder<K, V> implements CacheBuilder<K, V> {

    /**
     * 是否支持广播
     *
     * @return boolean
     */
    public abstract boolean supportBroadcast();

    /**
     * 创建广播管理;器
     *
     * @param cacheManager 缓存管理器
     * @return 广播管理器
     */
    public abstract BroadcastManager createBroadcastManager(CacheManager cacheManager);

    /**
     * 获取分布式缓存提供者
     *
     * @return 分布式缓存提供者
     */
    public abstract RemoteCacheProvider getProvider();
}
