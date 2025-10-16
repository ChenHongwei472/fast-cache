package cn.floseek.fastcache.cache;

import cn.floseek.fastcache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;

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
    protected CacheConfig<K, V> config;

    public AbstractCache(CacheConfig<K, V> config) {
        this.config = config;
    }

    @Override
    public V refresh(K key) {
        log.warn("Cache loader is not configured, please check your configuration.");
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public CacheConfig<K, V> getConfig() {
        return config;
    }

}
