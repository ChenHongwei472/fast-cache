package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.util.CacheUtils;

/**
 * 本地缓存抽象类
 *
 * @author ChenHongwei472
 */
public abstract class AbstractRemoteCache<K, V> extends AbstractCache<K, V> {

    public AbstractRemoteCache(CacheConfig config) {
        super(config);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.REMOTE;
    }

    /**
     * 获取缓存键
     *
     * @param key 键
     * @return 缓存键
     */
    public String getCacheKey(K key) {
        return CacheUtils.generateKey(config.getCacheName(), key.toString());
    }
}
