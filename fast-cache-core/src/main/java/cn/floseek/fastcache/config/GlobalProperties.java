package cn.floseek.fastcache.config;

import cn.floseek.fastcache.converter.KeyConverterType;
import cn.floseek.fastcache.common.enums.LocalCacheProvider;
import cn.floseek.fastcache.common.enums.RemoteCacheProvider;
import cn.floseek.fastcache.common.enums.SyncStrategy;
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
     * 缓存同步策略
     */
    private SyncStrategy syncStrategy = SyncStrategy.NONE;

    /**
     * 本地缓存配置
     */
    private LocalCache local = new LocalCache();

    /**
     * 分布式缓存配置
     */
    private RemoteCache remote = new RemoteCache();

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

    @Data
    public static class RemoteCache {

        /**
         * 提供者
         */
        private RemoteCacheProvider provider = RemoteCacheProvider.REDISSON;

        /**
         * 广播频道
         */
        private String broadcastChannel = "default_broadcast_channel";

        /**
         * 键名转换器
         */
        private KeyConverterType keyConverter = KeyConverterType.JACKSON;

        /**
         * 值序列化器
         */
        private ValueSerializerType serializer = ValueSerializerType.JAVA;

    }

}
