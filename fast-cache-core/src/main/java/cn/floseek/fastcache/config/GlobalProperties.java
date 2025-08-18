package cn.floseek.fastcache.config;

import cn.floseek.fastcache.cache.config.LocalCacheProvider;
import cn.floseek.fastcache.cache.config.RemoteCacheProvider;
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

    @Data
    public static class LocalCache {

        /**
         * 提供者
         */
        private LocalCacheProvider provider = LocalCacheProvider.CAFFEINE;
    }

    @Data
    public static class RemoteCache {

        /**
         * 提供者
         */
        private RemoteCacheProvider provider = RemoteCacheProvider.REDISSON;

        /**
         * 广播通道
         */
        private String broadcastChannel = "default_broadcast_channel";
    }
}
