package cn.floseek.fastcache.service;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheLoader;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.cache.config.RefreshPolicy;
import cn.floseek.fastcache.cache.config.SyncStrategy;
import cn.floseek.fastcache.cache.converter.KeyConverter;
import cn.floseek.fastcache.cache.serializer.Serializer;
import cn.floseek.fastcache.common.BaseCacheKey;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 抽象缓存服务实现
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public abstract class AbstractCacheService<K, V> implements CacheService<K, V> {

    private final Cache<K, V> cache;

    protected AbstractCacheService(CacheManager cacheManager) {
        CacheConfig<K, V> cacheConfig = this.buildCacheConfig();
        this.cache = cacheManager.getOrCreateCache(cacheConfig);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public List<V> listAll(Collection<? extends K> keys) {
        return cache.listAll(keys);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        cache.removeAll(keys);
    }

    /**
     * 获取缓存键基础接口
     *
     * @return 缓存键基础接口
     */
    protected abstract BaseCacheKey baseCacheKey();

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    protected String cacheName() {
        return this.baseCacheKey().getName();
    }

    /**
     * 获取缓存类型
     *
     * @return 缓存类型
     */
    protected abstract CacheType cacheType();

    /**
     * 获取缓存过期时间
     *
     * @return 缓存过期时间
     */
    protected Duration expireTime() {
        return this.baseCacheKey().getExpireTime();
    }

    /**
     * 获取本地缓存过期时间
     *
     * @return 本地缓存过期时间
     */
    protected Duration localExpireTime() {
        return this.baseCacheKey().getLocalExpireTime();
    }

    /**
     * 获取本地缓存最大容量
     *
     * @return 本地缓存最大容量
     */
    protected Long localMaximumSize() {
        return null;
    }

    /**
     * 获取缓存同步策略
     *
     * @return 缓存同步策略
     */
    protected SyncStrategy syncStrategy() {
        return null;
    }

    /**
     * 获取缓存刷新策略
     *
     * @return 缓存刷新策略
     */
    protected RefreshPolicy refreshPolicy() {
        return null;
    }

    /**
     * 获取键名转换器
     *
     * @return 键名转换器
     */
    protected KeyConverter keyConverter() {
        return null;
    }

    /**
     * 获取序列器
     *
     * @return 序列器
     */
    protected Serializer serializer() {
        return null;
    }

    /**
     * 获取查询方法
     *
     * @return 查询方法
     */
    protected Function<K, V> query() {
        return null;
    }

    /**
     * 获取批量查询方法
     *
     * @return 批量查询方法
     */
    protected Function<Collection<K>, Map<K, V>> queryAll() {
        return null;
    }

    /**
     * 构建缓存配置
     *
     * @return 缓存配置对象
     */
    private CacheConfig<K, V> buildCacheConfig() {
        CacheConfig<Object, Object> cacheConfig = CacheConfig.newBuilder(this.cacheName())
                .cacheType(this.cacheType())
                .expireTime(this.expireTime())
                .localExpireTime(this.localExpireTime())
                .localMaximumSize(this.localMaximumSize())
                .syncStrategy(this.syncStrategy())
                .refreshPolicy(this.refreshPolicy())
                .keyConverter(this.keyConverter())
                .serializer(this.serializer());

        if (Objects.isNull(this.query())) {
            return cacheConfig.build();
        }

        CacheLoader<K, V> cacheLoader = this.buildCacheLoader();
        return cacheConfig.build(cacheLoader);
    }

    /**
     * 构建缓存加载器
     *
     * @return 缓存加载器
     */
    private CacheLoader<K, V> buildCacheLoader() {
        CacheLoader<K, V> cacheLoader = key -> this.query().apply(key);
        if (Objects.nonNull(this.queryAll())) {
            cacheLoader = new CacheLoader<>() {
                @Override
                public V load(K key) {
                    if (Objects.isNull(key)) {
                        return null;
                    }

                    return query().apply(key);
                }

                @Override
                public Map<K, V> loadAll(Collection<K> keys) {
                    if (CollectionUtils.isEmpty(keys)) {
                        return Collections.emptyMap();
                    }

                    return queryAll().apply(keys);
                }
            };
        }
        return cacheLoader;
    }

}
