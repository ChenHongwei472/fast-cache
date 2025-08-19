package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheLoader;

import java.util.Objects;

/**
 * 缓存加载器装饰器
 * <p>
 * 主要用于在缓存未命中时，调用配置的缓存加载器加载缓存值
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class CacheLoaderDecorator<K, V> extends CacheDecorator<K, V> {

    private final CacheLoader<K, V> loader;
    private final boolean loaderEnabled;

    @SuppressWarnings("unchecked")
    public CacheLoaderDecorator(Cache<K, V> decoratedCache) {
        super(decoratedCache);
        CacheLoader<K, V> loader = (CacheLoader<K, V>) decoratedCache.getConfig().getLoader();
        this.loader = loader;
        this.loaderEnabled = Objects.nonNull(loader);
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (Objects.nonNull(value)) {
            return value;
        }

        if (loaderEnabled) {
            value = loader.apply(key);
            if (value != null) {
                super.put(key, value);
            }
        }
        return value;
    }
}
