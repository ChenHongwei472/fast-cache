package cn.floseek.fastcache.cache.builder;

import cn.floseek.fastcache.cache.config.LocalCacheProvider;
import cn.floseek.fastcache.cache.config.RemoteCacheProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存构建器管理器
 * <p>
 * 负责管理本地缓存和分布式缓存构建器，支持缓存构建器的注册和获取
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class CacheBuilderManager<K, V> {

    /**
     * 本地缓存构建器映射
     */
    private final Map<LocalCacheProvider, LocalCacheBuilder<K, V>> localCacheBuilderMap = new ConcurrentHashMap<>();

    /**
     * 分布式缓存构建器映射
     */
    private final Map<RemoteCacheProvider, RemoteCacheBuilder<K, V>> remoteCacheBuilderMap = new ConcurrentHashMap<>();

    /**
     * 注册本地缓存构建器
     *
     * @param provider 本地缓存提供者
     * @param builder  本地缓存构建器
     */
    public void registerLocalCacheBuilder(LocalCacheProvider provider, LocalCacheBuilder<K, V> builder) {
        localCacheBuilderMap.put(provider, builder);
    }

    /**
     * 获取本地缓存构建器
     *
     * @param provider 本地缓存提供者
     * @return 本地缓存构建器
     */
    public LocalCacheBuilder<K, V> getLocalCacheBuilder(LocalCacheProvider provider) {
        return localCacheBuilderMap.get(provider);
    }

    /**
     * 注册分布式缓存构建器
     *
     * @param provider 分布式缓存提供者
     * @param builder  分布式缓存构建器
     */
    public void registerRemoteCacheBuilder(RemoteCacheProvider provider, RemoteCacheBuilder<K, V> builder) {
        remoteCacheBuilderMap.put(provider, builder);
    }

    /**
     * 获取分布式缓存构建器
     *
     * @param provider 分布式缓存提供者
     * @return 分布式缓存构建器
     */
    public RemoteCacheBuilder<K, V> getRemoteCacheBuilder(RemoteCacheProvider provider) {
        return remoteCacheBuilderMap.get(provider);
    }
}
