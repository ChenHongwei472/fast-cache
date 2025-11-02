package cn.floseek.fastcache.config;

import cn.floseek.fastcache.common.enums.CacheSyncMode;
import cn.floseek.fastcache.common.enums.LocalCacheProvider;
import cn.floseek.fastcache.common.enums.RemoteCacheProvider;
import cn.floseek.fastcache.converter.KeyConverter;
import cn.floseek.fastcache.converter.KeyConverterType;
import cn.floseek.fastcache.serializer.ValueSerializer;
import cn.floseek.fastcache.serializer.ValueSerializerType;

/**
 * 全局配置属性
 *
 * @author ChenHongwei472
 */
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

    public LocalCache getLocal() {
        return local;
    }

    public void setLocal(LocalCache local) {
        this.local = local;
    }

    public RemoteCache getRemote() {
        return remote;
    }

    public void setRemote(RemoteCache remote) {
        this.remote = remote;
    }

    public CacheSyncStrategy getSyncStrategy() {
        return syncStrategy;
    }

    public void setSyncStrategy(CacheSyncStrategy syncStrategy) {
        this.syncStrategy = syncStrategy;
    }

    @Override
    public String toString() {
        return "GlobalProperties{" +
                "local=" + local +
                ", remote=" + remote +
                ", syncStrategy=" + syncStrategy +
                '}';
    }

    /**
     * 获取本地缓存提供者
     *
     * @return 本地缓存提供者
     */
    public LocalCacheProvider getLocalCacheProvider() {
        return this.local.getProvider();
    }

    /**
     * 获取本地缓存每个实例最大容量
     *
     * @return 每个实例最大容量
     */
    public Long getLocalCacheMaximumSize() {
        return this.local.getMaximumSize();
    }

    /**
     * 获取分布式缓存提供者
     *
     * @return 分布式缓存提供者
     */
    public RemoteCacheProvider getRemoteCacheProvider() {
        return this.remote.getProvider();
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

    /**
     * 本地缓存配置
     */
    public static class LocalCache {

        /**
         * 本地缓存提供者
         */
        private LocalCacheProvider provider = LocalCacheProvider.CAFFEINE;

        /**
         * 每个实例最大容量
         */
        private Long maximumSize;

        public LocalCacheProvider getProvider() {
            return provider;
        }

        public void setProvider(LocalCacheProvider provider) {
            this.provider = provider;
        }

        public Long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
        }

        @Override
        public String toString() {
            return "LocalCache{" +
                    "provider=" + provider +
                    ", maximumSize=" + maximumSize +
                    '}';
        }

    }

    /**
     * 分布式缓存配置
     */
    public static class RemoteCache {

        /**
         * 分布式缓存提供者
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

        public RemoteCacheProvider getProvider() {
            return provider;
        }

        public void setProvider(RemoteCacheProvider provider) {
            this.provider = provider;
        }

        public KeyConverterType getKeyConverter() {
            return keyConverter;
        }

        public void setKeyConverter(KeyConverterType keyConverter) {
            this.keyConverter = keyConverter;
        }

        public ValueSerializerType getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(ValueSerializerType valueSerializer) {
            this.valueSerializer = valueSerializer;
        }

        @Override
        public String toString() {
            return "RemoteCache{" +
                    "provider=" + provider +
                    ", keyConverter=" + keyConverter +
                    ", valueSerializer=" + valueSerializer +
                    '}';
        }

    }

    /**
     * 缓存同步策略配置
     */
    public static class CacheSyncStrategy {

        /**
         * 缓存同步模式
         */
        private CacheSyncMode mode = CacheSyncMode.NONE;

        /**
         * 缓存广播频道
         */
        private String broadcastChannel = "fast_cache_broadcast_channel";

        public CacheSyncMode getMode() {
            return mode;
        }

        public void setMode(CacheSyncMode mode) {
            this.mode = mode;
        }

        public String getBroadcastChannel() {
            return broadcastChannel;
        }

        public void setBroadcastChannel(String broadcastChannel) {
            this.broadcastChannel = broadcastChannel;
        }

        @Override
        public String toString() {
            return "CacheSyncStrategy{" +
                    "mode=" + mode +
                    ", broadcastChannel='" + broadcastChannel + '\'' +
                    '}';
        }

    }

}
