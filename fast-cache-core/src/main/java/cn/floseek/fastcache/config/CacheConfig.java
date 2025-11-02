package cn.floseek.fastcache.config;

import cn.floseek.fastcache.cache.CacheLoader;
import cn.floseek.fastcache.common.enums.BaseCacheKeyEnum;
import cn.floseek.fastcache.common.enums.CacheSyncMode;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.converter.KeyConverter;
import cn.floseek.fastcache.serializer.ValueSerializer;

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
     * 缓存同步模式
     */
    private CacheSyncMode cacheSyncMode;

    /**
     * 缓存刷新策略
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 键名转换器
     */
    private KeyConverter keyConverter;

    /**
     * 值序列器
     */
    private ValueSerializer valueSerializer;

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

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public Duration getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    public Duration getLocalExpireTime() {
        return localExpireTime;
    }

    public void setLocalExpireTime(Duration localExpireTime) {
        this.localExpireTime = localExpireTime;
    }

    public Long getLocalMaximumSize() {
        return localMaximumSize;
    }

    public void setLocalMaximumSize(Long localMaximumSize) {
        this.localMaximumSize = localMaximumSize;
    }

    public CacheSyncMode getCacheSyncMode() {
        return cacheSyncMode;
    }

    public void setCacheSyncMode(CacheSyncMode cacheSyncMode) {
        this.cacheSyncMode = cacheSyncMode;
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }

    public KeyConverter getKeyConverter() {
        return keyConverter;
    }

    public void setKeyConverter(KeyConverter keyConverter) {
        this.keyConverter = keyConverter;
    }

    public ValueSerializer getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(ValueSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public CacheLoader<K, V> getLoader() {
        return loader;
    }

    public void setLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "cacheName='" + cacheName + '\'' +
                ", cacheType=" + cacheType +
                ", expireTime=" + expireTime +
                ", localExpireTime=" + localExpireTime +
                ", localMaximumSize=" + localMaximumSize +
                ", cacheSyncMode=" + cacheSyncMode +
                ", refreshPolicy=" + refreshPolicy +
                ", keyConverter=" + keyConverter +
                ", valueSerializer=" + valueSerializer +
                ", loader=" + loader +
                '}';
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
     * 设置缓存同步模式
     *
     * @param cacheSyncMode 缓存同步模式
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> cacheSyncMode(CacheSyncMode cacheSyncMode) {
        this.cacheSyncMode = cacheSyncMode;
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
     * 设置值序列化器
     *
     * @param valueSerializer 值序列化器
     * @return 缓存配置对象
     */
    public CacheConfig<K, V> serializer(ValueSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
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
        return this.cacheSyncMode != CacheSyncMode.NONE;
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
