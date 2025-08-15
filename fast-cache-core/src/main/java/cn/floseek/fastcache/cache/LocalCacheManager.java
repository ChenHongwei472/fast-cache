package cn.floseek.fastcache.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存管理器
 *
 * @author ChenHongwei472
 */
public class LocalCacheManager {

    /**
     * 静态内部类实现单例模式
     */
    private static class SingletonHolder {
        private static final LocalCacheManager INSTANCE = new LocalCacheManager();
    }

    /**
     * 获取本地缓存管理器实例
     *
     * @return 本地缓存管理器实例
     */
    public static LocalCacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 私有构造函数，防止外部实例化
     */
    private LocalCacheManager() {
    }

    /**
     * 缓存映射，key：缓存名称，value：缓存实例
     */
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 注册缓存实例
     *
     * @param cacheName 缓存名称
     * @param cache     缓存实例
     * @param <K>       缓存键类型
     * @param <V>       缓存值类型
     */
    public <K, V> void registerCache(String cacheName, Cache<K, V> cache) {
        cacheMap.put(cacheName, cache);
    }

    /**
     * 获取或创建缓存实例
     *
     * @param cacheName   缓存名称
     * @param cacheConfig 缓存配置
     * @param <K>         缓存键类型
     * @param <V>         缓存值类型
     * @return 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getOrCreateCache(String cacheName, CacheConfig cacheConfig) {
        return (Cache<K, V>) cacheMap.computeIfAbsent(cacheName, k -> this.buildLocalCache(cacheConfig));
    }

    /**
     * 获取缓存实例
     *
     * @param cacheName 缓存名称
     * @param <K>       缓存键类型
     * @param <V>       缓存值类型
     * @return 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return (Cache<K, V>) cacheMap.get(cacheName);
    }

    /**
     * 构建本地缓存
     *
     * @param cacheConfig 缓存配置
     * @return 本地缓存对象
     */
    private <K, V> Cache<K, V> buildLocalCache(CacheConfig cacheConfig) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getMaximumSize());

        if (cacheConfig.getExpireAfterWrite() != null) {
            caffeine.expireAfterWrite(cacheConfig.getExpireAfterWrite());
        }

        if (cacheConfig.getExpireAfterAccess() != null) {
            caffeine.expireAfterAccess(cacheConfig.getExpireAfterAccess());
        }

        return caffeine.build();
    }
}
