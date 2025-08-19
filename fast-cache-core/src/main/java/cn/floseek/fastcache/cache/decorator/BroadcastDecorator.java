package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.cache.broadcast.BroadcastMessage;
import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 广播装饰器
 * <p>
 * 为缓存实例添加广播功能，用于实现分布式环境下的缓存同步
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Slf4j
public class BroadcastDecorator<K, V> extends CacheDecorator<K, V> {

    public BroadcastDecorator(Cache<K, V> decoratedCache) {
        super(decoratedCache);
    }

    @Override
    public void put(K key, V value) {
        super.put(key, value);
        this.broadcast(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        this.broadcast(map.keySet());
    }

    @Override
    public void remove(K key) {
        super.remove(key);
        this.broadcast(key);
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        super.removeAll(keys);
        this.broadcast(keys);
    }

    /**
     * 广播单个键
     *
     * @param key 缓存键
     */
    private void broadcast(K key) {
        this.broadcast(List.of(key));
    }

    /**
     * 广播多个键
     *
     * @param keys 缓存键集合
     */
    private void broadcast(Collection<? extends K> keys) {
        CacheConfig config = getConfig();
        BroadcastManager broadcastManager = config.getBroadcastManager();

        // 参数校验
        if (CollectionUtils.isEmpty(keys)) {
            log.debug("Skip broadcast for empty key set");
            return;
        }
        if (!config.isSyncCache()) {
            log.debug("Cache sync is disabled, skip broadcast");
            return;
        }
        if (broadcastManager == null) {
            log.debug("BroadcastManager not is configured, skip broadcast");
            return;
        }

        // 创建广播消息
        BroadcastMessage broadcastMessage = BroadcastMessage.builder()
                .instanceId(broadcastManager.getInstanceId())
                .cacheName(config.getCacheName())
                .keys(new ArrayList<>(keys))
                .build();

        // 发送广播
        broadcastManager.publish(broadcastMessage);
        log.debug("Send broadcast message success, cacheName: {}, keys: {}", config.getCacheName(), keys);
    }
}
