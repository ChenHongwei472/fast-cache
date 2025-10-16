package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.config.GlobalProperties;

/**
 * 缓存管理器接口
 * <p>
 * 负责缓存的创建、获取和管理
 * </p>
 *
 * @author ChenHongwei472
 */
public interface CacheManager extends AutoCloseable {

    /**
     * 获取或创建缓存实例
     *
     * @param config 缓存配置对象
     * @param <K>    缓存键类型
     * @param <V>    缓存值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getOrCreateCache(CacheConfig<K, V> config);

    /**
     * 获取缓存实例
     *
     * @param cacheType 缓存类型
     * @param cacheName 缓存名称
     * @param <K>       缓存键类型
     * @param <V>       缓存值类型
     * @return 缓存实例
     */
    <K, V> Cache<K, V> getCache(CacheType cacheType, String cacheName);

    /**
     * 获取全局配置
     *
     * @return 全局配置对象
     */
    GlobalProperties getGlobalProperties();

}
