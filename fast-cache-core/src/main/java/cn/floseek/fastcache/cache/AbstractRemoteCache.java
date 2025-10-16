package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.common.enums.CacheType;
import cn.floseek.fastcache.util.CacheUtils;

import java.nio.charset.Charset;

/**
 * 本地缓存抽象类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public abstract class AbstractRemoteCache<K, V> extends AbstractCache<K, V> {

    public AbstractRemoteCache(CacheConfig<K, V> config) {
        super(config);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.REMOTE;
    }

    /**
     * 构建缓存键
     *
     * @param key 缓存键
     * @return 字节数组
     */
    public byte[] buildCacheKey(K key) {
        String cacheKey = CacheUtils.generateKey(config.getCacheName(), config.getKeyConverter().convert(key));
        return cacheKey.getBytes(Charset.defaultCharset());
    }

}
