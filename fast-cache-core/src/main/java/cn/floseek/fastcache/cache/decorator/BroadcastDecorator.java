package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.broadcast.BroadcastMessage;
import cn.floseek.fastcache.common.enums.CacheSyncMode;
import cn.floseek.fastcache.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广播装饰器
 * <p>
 * 为缓存实例添加广播功能，用于实现分布式环境下的本地缓存数据同步
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class BroadcastDecorator<K, V> extends CacheDecorator<K, V> {

    private static final Logger log = LoggerFactory.getLogger(BroadcastDecorator.class);

    protected final CacheConfig<K, V> config;
    private final BroadcastManager broadcastManager;

    public BroadcastDecorator(Cache<K, V> decoratedCache, BroadcastManager broadcastManager) {
        super(decoratedCache);
        this.config = decoratedCache.getConfig();
        this.broadcastManager = broadcastManager;
    }

    @Override
    public void put(K key, V value) {
        super.put(key, value);
        this.notifyUpdateOrInvalidate(Map.of(key, value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        this.notifyUpdateOrInvalidate(map);
    }

    @Override
    public void remove(K key) {
        super.remove(key);
        this.notifyInvalidate(List.of(key));
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        super.removeAll(keys);
        this.notifyInvalidate(keys);
    }

    /**
     * 通知更新或删除缓存
     *
     * @param keyValues 缓存键值对
     */
    private void notifyUpdateOrInvalidate(Map<? extends K, ? extends V> keyValues) {
        // 判断是否跳过广播
        if (this.skipBroadcast()) {
            return;
        }

        if (config.getCacheSyncMode() == CacheSyncMode.UPDATE) {
            this.notifyUpdate(keyValues);
        } else if (config.getCacheSyncMode() == CacheSyncMode.INVALIDATE) {
            this.notifyInvalidate(keyValues.keySet());
        }
    }

    /**
     * 通知更新缓存
     *
     * @param keyValues 缓存键值对
     */
    private void notifyUpdate(Map<? extends K, ? extends V> keyValues) {
        // 判断是否跳过广播
        if (this.skipBroadcast()) {
            return;
        }

        // 创建广播消息
        BroadcastMessage broadcastMessage = BroadcastMessage.buildUpdate(
                broadcastManager.getInstanceId(), config.getCacheName(), new HashMap<>(keyValues));

        // 发送广播消息
        broadcastManager.publish(broadcastMessage);
        log.debug("Send update broadcast message success, cacheName: {}, message: {}", config.getCacheName(), broadcastMessage);
    }

    /**
     * 通知删除缓存
     *
     * @param keys 缓存键集合
     */
    private void notifyInvalidate(Collection<? extends K> keys) {
        // 判断是否跳过广播
        if (this.skipBroadcast()) {
            return;
        }

        // 创建广播消息
        BroadcastMessage broadcastMessage = BroadcastMessage.buildInvalidate(
                broadcastManager.getInstanceId(), config.getCacheName(), new ArrayList<>(keys));

        // 发送广播消息
        broadcastManager.publish(broadcastMessage);
        log.debug("Send invalidate broadcast message success, cacheName: {}, message: {}", config.getCacheName(), broadcastMessage);
    }

    /**
     * 判断是否跳过广播
     *
     * @return boolean
     */
    private boolean skipBroadcast() {
        if (broadcastManager == null) {
            log.debug("Broadcast manager not configured, skip broadcast");
            return true;
        }
        if (config.getCacheSyncMode() == CacheSyncMode.NONE) {
            log.debug("Cache sync mode is NONE, skip broadcast");
            return true;
        }
        return false;
    }

}
