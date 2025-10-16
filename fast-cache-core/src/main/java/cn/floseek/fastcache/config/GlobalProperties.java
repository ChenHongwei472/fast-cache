package cn.floseek.fastcache.config;

import cn.floseek.fastcache.common.enums.LocalCacheProvider;
import cn.floseek.fastcache.common.enums.RemoteCacheProvider;
import cn.floseek.fastcache.common.enums.CacheSyncMode;
import cn.floseek.fastcache.converter.KeyConverter;
import cn.floseek.fastcache.converter.KeyConverterType;
import cn.floseek.fastcache.serializer.ValueSerializer;
import cn.floseek.fastcache.serializer.ValueSerializerType;
import lombok.Data;

/**
 * 全局配置属性
 *
 * @author ChenHongwei472
 */
@Data
public class GlobalProperties {

    /**
     * 本地缓存配置
     */
    private LocalCache local = new LocalCache();

    /**
     * 分布式缓存配置
     */
    private RemoteCache remote = new RemoteCache();

    /**
     * 缓存同步策略配置
     */
    private CacheSyncStrategy syncStrategy = new CacheSyncStrategy();

    /**
     * 本地缓存配置
     */
    @Data
    public static class LocalCache {

        /**
         * 提供者
         */
        private LocalCacheProvider provider = LocalCacheProvider.CAFFEINE;

        /**
         * 每个实例最大容量
         */
        private Long maximumSize;

    }

    /**
     * 分布式缓存配置
     */
    @Data
    public static class RemoteCache {

        /**
         * 提供者
         */
        private RemoteCacheProvider provider = RemoteCacheProvider.REDISSON;

        /**
         * 键名转换器类型
         */
        private KeyConverterType keyConverter = KeyConverterType.JACKSON;

        /**
         * 值序列化器类型
         */
        private ValueSerializerType valueSerializer = ValueSerializerType.JAVA;

    }

    /**
     * 缓存同步策略配置
     */
    @Data
    public static class CacheSyncStrategy {

        /**
         * 缓存同步模式
         */
        private CacheSyncMode mode = CacheSyncMode.NONE;

        /**
         * 缓存广播频道
         */
        private String broadcastChannel = "fast_cache_broadcast_channel";

    }

    /**
     * 获取分布式缓存键名转换器
     *
     * @return 键名转换器
     */
    public KeyConverter getRemoteCacheKeyConverter() {
        return this.remote.getKeyConverter().getInstance();
    }

    /**
     * 获取分布式缓存值序列化器
     *
     * @return 值序列化器
     */
    public ValueSerializer getRemoteCacheValueSerializer() {
        return this.remote.getValueSerializer().getInstance();
    }

    /**
     * 获取缓存同步模式
     *
     * @return 缓存同步模式
     */
    public CacheSyncMode getCacheSyncMode() {
        return this.syncStrategy.getMode();
    }

    /**
     * 获取缓存同步广播频道
     *
     * @return 广播频道
     */
    public String getCacheSyncBroadcastChannel() {
        return this.syncStrategy.getBroadcastChannel();
    }

}
