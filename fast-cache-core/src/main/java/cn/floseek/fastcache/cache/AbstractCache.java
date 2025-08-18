package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 缓存抽象类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    /**
     * 缓存配置
     */
    protected CacheConfig config;

    public AbstractCache(CacheConfig config) {
        this.config = config;
    }

    @Override
    public V get(K key, Supplier<V> valueLoader) {
        // 首先尝试从缓存中获取数据
        V value = this.get(key);
        if (value != null) {
            return value;
        }

        // 缓存未命中，则从数据源中加载数据
        value = valueLoader.get();
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
