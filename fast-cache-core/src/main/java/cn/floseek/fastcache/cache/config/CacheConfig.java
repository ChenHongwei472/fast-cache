package cn.floseek.fastcache.cache.config;

import cn.floseek.fastcache.cache.converter.KeyConverter;
import cn.floseek.fastcache.cache.converter.KeyConverterType;
import cn.floseek.fastcache.cache.serialize.Serializer;
import cn.floseek.fastcache.cache.serialize.SerializerType;
import cn.floseek.fastcache.common.BaseCacheKey;
import lombok.Getter;
import lombok.Setter;

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
@Setter
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
     * 是否同步本地缓存
     */
    private Boolean syncLocal;

    /**
     * 缓存刷新策略
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 缓存加载器
     */
    private CacheLoader<K, V> loader;

    /**
     * 键名转换器
     */
    private KeyConverter keyConverter = KeyConverterType.JACKSON.getKeyConverter();

    /**
     * 序列器
     */
    private Serializer serializer = SerializerType.JAVA.getSerializer();

    /**
     * 创建缓存配置
     *
     * @param cacheName 缓存名称
     * @param <K>       缓存键类型
     * @param <V>       缓存值类型
     * @return 缓存配置构建器
     */
    public static <K, V> CacheConfigBuilder<K, V> newBuilder(String cacheName) {
        return new CacheConfigBuilder<>(cacheName);
    }

    /**
     * 创建缓存配置
     *
     * @param baseCacheKey 缓存枚举
     * @param <K>          缓存键类型
     * @param <V>          缓存值类型
     * @return 缓存配置构建器
     */
    public static <K, V> CacheConfigBuilder<K, V> newBuilder(BaseCacheKey baseCacheKey) {
        return new CacheConfigBuilder<>(baseCacheKey);
    }

    /**
     * 是否启用缓存加载器
     *
     * @return boolean
     */
    public boolean loaderEnabled() {
        return Objects.nonNull(this.loader);
    }

    /**
     * 缓存配置构建器
     *
     * @param <K> 缓存键类型
     * @param <V> 缓存值类型
     */
    public static class CacheConfigBuilder<K, V> {

        /**
         * 缓存名称
         */
        private final String cacheName;

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
         * 是否同步本地缓存
         */
        private Boolean syncLocal;

        /**
         * 缓存刷新策略
         */
        private RefreshPolicy refreshPolicy;

        /**
         * 缓存加载器
         */
        private CacheLoader<K, V> loader;

        /**
         * 键名转换器
         */
        private KeyConverter keyConverter = KeyConverterType.JACKSON.getKeyConverter();

        /**
         * 序列化器
         */
        private Serializer serializer = SerializerType.JAVA.getSerializer();

        public CacheConfigBuilder(String cacheName) {
            Objects.requireNonNull(cacheName);
            this.cacheName = cacheName;
        }

        public CacheConfigBuilder(BaseCacheKey baseCacheKey) {
            Objects.requireNonNull(baseCacheKey.getName());
            this.cacheName = baseCacheKey.getName();
            this.expireTime = baseCacheKey.getExpireTime();
            this.localExpireTime = baseCacheKey.getLocalExpireTime();
        }

        public CacheConfigBuilder<K, V> cacheType(CacheType cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public CacheConfigBuilder<K, V> expireTime(Duration expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public CacheConfigBuilder<K, V> expireTime(long time, TimeUnit timeUnit) {
            this.expireTime = Duration.ofMillis(timeUnit.toMillis(time));
            return this;
        }

        public CacheConfigBuilder<K, V> localExpireTime(Duration localExpireTime) {
            this.localExpireTime = localExpireTime;
            return this;
        }

        public CacheConfigBuilder<K, V> localExpireTime(long time, TimeUnit timeUnit) {
            this.localExpireTime = Duration.ofMillis(timeUnit.toMillis(time));
            return this;
        }

        public CacheConfigBuilder<K, V> localMaximumSize(Long localMaximumSize) {
            this.localMaximumSize = localMaximumSize;
            return this;
        }

        public CacheConfigBuilder<K, V> syncLocal(Boolean syncLocal) {
            this.syncLocal = syncLocal;
            return this;
        }

        public CacheConfigBuilder<K, V> refreshPolicy(RefreshPolicy refreshPolicy) {
            this.refreshPolicy = refreshPolicy;
            return this;
        }

        public CacheConfigBuilder<K, V> loader(CacheLoader<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public CacheConfigBuilder<K, V> keyConverter(KeyConverter keyConverter) {
            this.keyConverter = keyConverter;
            return this;
        }

        public CacheConfigBuilder<K, V> serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public CacheConfig<K, V> build() {
            CacheConfig<K, V> cacheConfig = new CacheConfig<>();
            cacheConfig.cacheName = cacheName;
            cacheConfig.cacheType = cacheType;
            cacheConfig.expireTime = expireTime;
            cacheConfig.localExpireTime = localExpireTime;
            cacheConfig.localMaximumSize = localMaximumSize;
            cacheConfig.syncLocal = syncLocal;
            cacheConfig.refreshPolicy = refreshPolicy;
            cacheConfig.loader = loader;
            cacheConfig.keyConverter = keyConverter;
            cacheConfig.serializer = serializer;
            return cacheConfig;
        }
    }
}
