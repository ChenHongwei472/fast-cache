package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.util.CacheUtils;

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
     * 生成分布式缓存键
     *
     * @param key 键
     * @return 分布式缓存键
     */
    public String generateCacheKey(K key) {
        return CacheUtils.generateKey(config.getCacheName(), key.toString());
    }
}
