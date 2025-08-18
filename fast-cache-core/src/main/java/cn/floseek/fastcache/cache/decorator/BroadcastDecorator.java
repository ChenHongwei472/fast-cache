package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.broadcast.CacheMessage;
import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import cn.floseek.fastcache.cache.config.CacheType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 广播装饰器
 *
 * @author ChenHongwei472
 */
@Slf4j
public class BroadcastDecorator<K, V> extends CacheDecorator<K, V> {

    public BroadcastDecorator(Cache<K, V> cache) {
        super(cache);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public V get(K key, Supplier<V> dbLoader) {
        return cache.get(key, dbLoader);
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        this.broadcast(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
        this.broadcast(map.keySet());
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
        this.broadcast(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        cache.removeAll(keys);
        this.broadcast(keys);
    }

    @Override
    public CacheType getCacheType() {
        return cache.getCacheType();
    }

    private void broadcast(K key) {
        this.broadcast(List.of(key));
    }

    private void broadcast(Collection<? extends K> keys) {
        CacheConfig config = getConfig();
        log.info("开始发布广播消息，缓存名称：{}，keys：{}", config.getCacheName(), keys);
        if (!config.isSyncLocalCache()) {
            log.info("当前配置不同步本地缓存，不进行广播");
            return;
        }

        if (config.getBroadcastManager() == null) {
            log.info("当前广播管理器为空，不进行广播");
            return;
        }

        BroadcastManager broadcastManager = config.getBroadcastManager();
        CacheMessage cacheMessage = CacheMessage.builder()
                .instanceId(broadcastManager.getInstanceId())
                .cacheName(config.getCacheName())
                .keys(new ArrayList<>(keys))
                .build();
        broadcastManager.publish(cacheMessage);
    }
}
