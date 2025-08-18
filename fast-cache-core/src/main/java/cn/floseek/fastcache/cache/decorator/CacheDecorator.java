package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存装饰器抽象类
 * <p>
 * 提供装饰器模式的基本实现，实现对缓存实例的装饰，继承该抽象类实现具体的装饰逻辑
 * </p>
 *
 * @author ChenHongwei472
 */
@Slf4j
public abstract class CacheDecorator<K, V> implements Cache<K, V> {

    /**
     * 被装饰的缓存实例
     */
    protected final Cache<K, V> decoratedCache;

    public CacheDecorator(Cache<K, V> decoratedCache) {
        this.decoratedCache = decoratedCache;
    }

    @Override
    public CacheConfig getConfig() {
        return decoratedCache.getConfig();
    }

    /**
     * 获取被装饰的缓存实例
     *
     * @return 缓存实例
     */
    public Cache<K, V> unwrap() {
        return decoratedCache;
    }

    /**
     * 获取被装饰的原始缓存实例
     *
     * @return 缓存实例
     */
    public Cache<K, V> unwrapAll() {
        Cache<K, V> current = decoratedCache;
        while (current instanceof CacheDecorator) {
            current = ((CacheDecorator<K, V>) current).unwrapAll();
        }
        return current;
    }

    /**
     * 检查装饰链中是否包含指定类型的装饰器
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
