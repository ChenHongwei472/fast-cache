package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 缓存抽象类
 *
 * @author ChenHongwei472
 */
@Slf4j
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    protected CacheConfig config;

    public AbstractCache(CacheConfig config) {
        this.config = config;
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        V value = this.get(key);
        if (value != null) {
            return value;
        }

        value = dbLoader.get();
        if (value != null) {
            this.put(key, value);
        }
        return value;
    }

    @Override
    public CacheConfig getConfig() {
        return config;
    }
}
