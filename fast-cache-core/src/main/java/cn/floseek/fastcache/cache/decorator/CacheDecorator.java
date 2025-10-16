package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.common.enums.CacheType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * 缓存装饰器抽象类
 * <p>
 * 提供装饰器模式的基本实现，实现对缓存实例的装饰，继承该抽象类实现具体的装饰逻辑
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
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
    public V get(K key) {
        return decoratedCache.get(key);
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        return decoratedCache.getAll(keys);
    }

    @Override
    public void put(K key, V value) {
        decoratedCache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        decoratedCache.putAll(map);
    }

    @Override
    public void remove(K key) {
        decoratedCache.remove(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        decoratedCache.removeAll(keys);
    }

    @Override
    public V refresh(K key) {
        return decoratedCache.refresh(key);
    }

    @Override
    public void close() {
        decoratedCache.close();
    }

    @Override
    public CacheType getCacheType() {
        return decoratedCache.getCacheType();
    }

    @Override
    public CacheConfig<K, V> getConfig() {
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
