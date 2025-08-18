package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存装饰器
 *
 * @author ChenHongwei472
 */
@Slf4j
public abstract class CacheDecorator<K, V> implements Cache<K, V> {

    protected final Cache<K, V> cache;

    public CacheDecorator(Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public CacheConfig getConfig() {
        return cache.getConfig();
    }

    /**
     * 获取被装饰的缓存实例
     *
     * @return 缓存实例
     */
    public Cache<K, V> unwrap() {
        return cache;
    }

    /**
     * 获取最原始被装饰的缓存实例
     *
     * @return 缓存实例
     */
    public Cache<K, V> unwrapAll() {
        Cache<K, V> current = cache;
        while (current instanceof CacheDecorator) {
            current = ((CacheDecorator<K, V>) current).unwrapAll();
        }
        return current;
    }

    /**
     * 检查是否包含特定类型的装饰器
     *
     * @param clazz 装饰器类
     * @return boolean
     */
    public <T extends CacheDecorator<?, ?>> boolean containsDecorator(Class<T> clazz) {
        Cache<K, V> current = this;
        while (current instanceof CacheDecorator) {
            if (clazz.isInstance(current)) {
                return true;
            }
            current = ((CacheDecorator<K, V>) current).unwrap();
        }
        return false;
    }
}
