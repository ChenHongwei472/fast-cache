package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;

/**
 * 本地缓存抽象类
 *
 * @author ChenHongwei472
 */
public abstract class AbstractLocalCache<K, V> extends AbstractCache<K, V> {

    public AbstractLocalCache(CacheConfig config) {
        super(config);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.LOCAL;
    }
}
