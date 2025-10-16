package cn.floseek.fastcache.config;

import cn.floseek.fastcache.cache.config.CacheLoader;
import cn.floseek.fastcache.converter.KeyConverter;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.common.enums.SyncStrategy;
import cn.floseek.fastcache.serializer.Serializer;
import cn.floseek.fastcache.common.enums.BaseCacheKeyEnum;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Getter
@ToString
public class CacheConfig<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存类型
     */
    private CacheType cacheType = CacheType.REMOTE;

    /**
     * 缓存过期时间
     */
    private Duration expireTime;

    /**
     * 本地缓存过期时间
     */
    private Duration localExpireTime;

    /**
     * 本地缓存最大容量
     */
    private Long localMaximumSize;

    /**
     * 缓存同步策略
     */
    private SyncStrategy syncStrategy;

    /**
     * 缓存刷新策略
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 键名转换器
     */
    private KeyConverter keyConverter;

    /**
     * 序列器
     */
    private Serializer serializer;

    /**
     * 缓存加载器
     */
    private CacheLoader<K, V> loader;

    private CacheConfig() {
    }

    private CacheConfig(String cacheName) {
        Objects.requireNonNull(cacheName);
        this.cacheName = cacheName;
    }

    private CacheConfig(BaseCacheKeyEnum baseCacheKeyEnum) {
        Objects.requireNonNull(baseCacheKeyEnum.getName());
        this.cacheName = baseCacheKeyEnum.getName();
        this.expireTime = baseCacheKeyEnum.getExpireTime();
        this.localExpireTime = baseCacheKeyEnum.getLocalExpireTime();
    }

    /**
     * 创建缓存配置
     *
     * @param cacheName 缓存名称
     * @return 缓存配置对象
     */
    public static CacheConfig<Object, Object> newBuilder(String cacheName) {
        return new CacheConfig<>(cacheName);
    }

    /**
     * 创建缓存配置
     *
     * @param baseCacheKeyEnum 缓存键枚举
     * @return 缓存配置对象
     */
    public static CacheConfig<Object, Object> newBuilder(BaseCacheKeyEnum baseCacheKeyEnum) {
        return new CacheConfig<>(baseCacheKeyEnum);
    }

    /**
     * 设置缓存类型
     *
     * @param cacheType 缓存类型枚举
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> cacheType(CacheType cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    /**
     * 设置缓存过期时间
     *
     * @param expireTime 缓存过期时间
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> expireTime(Duration expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    /**
     * 缓存过期时间
     *
     * @param time     时间
     * @param timeUnit 时间单位
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> expireTime(long time, TimeUnit timeUnit) {
        this.expireTime = Duration.ofMillis(timeUnit.toMillis(time));
        return this;
    }

    /**
     * 设置本地缓存过期时间
     *
     * @param localExpireTime 本地缓存过期时间
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> localExpireTime(Duration localExpireTime) {
        this.localExpireTime = localExpireTime;
        return this;
    }

    /**
     * 添加本地缓存过期时间
     *
     * @param time     时间
     * @param timeUnit 时间单位
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> localExpireTime(long time, TimeUnit timeUnit) {
        this.localExpireTime = Duration.ofMillis(timeUnit.toMillis(time));
        return this;
    }

    /**
     * 设置本地缓存最大数量
     *
     * @param localMaximumSize 本地缓存最大数量
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> localMaximumSize(Long localMaximumSize) {
        this.localMaximumSize = localMaximumSize;
        return this;
    }

    /**
     * 设置缓存同步策略
     *
     * @param syncStrategy 缓存同步策略
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> syncStrategy(SyncStrategy syncStrategy) {
        this.syncStrategy = syncStrategy;
        return this;
    }

    /**
     * 设置缓存刷新策略
     *
     * @param refreshPolicy 缓存刷新策略
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> refreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
        return this;
    }

    /**
     * 设置键名转换器
     *
     * @param keyConverter 键名转换器
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> keyConverter(KeyConverter keyConverter) {
        this.keyConverter = keyConverter;
        return this;
    }

    /**
     * 缓存序列化器
     *
     * @param serializer 序列化器
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * 构建缓存配置对象
     *
     * @param <K1> 缓存键类型
     * @param <V1> 缓存值类型
     * @return 缓存配置对象
     */
    @SuppressWarnings("unchecked")
    public <K1 extends K, V1 extends V> CacheConfig<K1, V1> build() {
        return (CacheConfig<K1, V1>) this;
    }

    /**
     * 构建缓存配置对象
     *
     * @param <K1>   缓存键类型
     * @param <V1>   缓存值类型
     * @param loader 缓存加载器
     * @return 缓存配置对象
     */
    public <K1 extends K, V1 extends V> CacheConfig<K1, V1> build(CacheLoader<K1, V1> loader) {
        CacheConfig<K1, V1> cacheConfig = this.build();
        cacheConfig.loader = loader;
        return cacheConfig;
    }

    /**
     * 是否启用缓存同步
     *
     * @return boolean
     */
    public boolean isSyncEnabled() {
        return this.syncStrategy != SyncStrategy.NONE;
    }

    /**
     * 是否启用缓存加载器
     *
     * @return boolean
     */
    public boolean loaderEnabled() {
        return Objects.nonNull(this.loader);
    }

}
