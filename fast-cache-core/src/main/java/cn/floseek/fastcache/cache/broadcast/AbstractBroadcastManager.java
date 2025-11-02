package cn.floseek.fastcache.cache.broadcast;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.decorator.BroadcastDecorator;
import cn.floseek.fastcache.cache.decorator.CacheDecorator;
import cn.floseek.fastcache.cache.multilevel.MultiLevelCache;
import cn.floseek.fastcache.common.enums.CacheType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 广播管理器抽象类
 * <p>
 * 提供广播消息处理的基本实现，具体广播机制由子类实现
 * </p>
 *
 * @author ChenHongwei472
 */
public abstract class AbstractBroadcastManager implements BroadcastManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractBroadcastManager.class);

    /**
     * 当前实例 ID
     */
    private final String instanceId = UUID.randomUUID().toString();

    private final CacheManager cacheManager;

    protected AbstractBroadcastManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 处理接收到的广播消息
     *
     * @param broadcastMessage 广播消息对象
     */
    protected void processMessage(BroadcastMessage broadcastMessage) {
        // 验证消息是否为空
        if (broadcastMessage == null) {
            log.debug("Received empty broadcast message, skip processing");
            return;
        }
        log.debug("Received broadcast message: {}", broadcastMessage);

        // 忽略本实例发出的消息
        if (this.instanceId.equals(broadcastMessage.getInstanceId())) {
            log.debug("Received broadcast message from current instance, skip processing");
            return;
        }

        // 获取被装饰的缓存实例
        CacheDecorator<Object, Object> cacheDecorator = null;
        for (CacheType value : CacheType.values()) {
            Cache<Object, Object> cache = cacheManager.getCache(value, broadcastMessage.getCacheName());
            if (cache instanceof CacheDecorator<Object, Object>) {
                cacheDecorator = (CacheDecorator<Object, Object>) cache;
                break;
            }
        }

        // 检查缓存实例是否存在
        if (cacheDecorator == null) {
            log.warn("Cache does not exist: {}", broadcastMessage.getCacheName());
            return;
        }

        // 检查缓存实例是否支持广播
        if (!cacheDecorator.containsDecorator(BroadcastDecorator.class)) {
            log.debug("Cache does not support broadcast: {}", broadcastMessage.getCacheName());
            return;
        }

        // 获取被装饰的原始缓存实例
        Cache<Object, Object> cache = cacheDecorator.unwrapAll();
        if (cache.getCacheType() == CacheType.REMOTE) {
            log.debug("Skip broadcast for remote cache: {}", broadcastMessage.getCacheName());
            return;
        }

        // 获取本地缓存实例
        Cache<Object, Object> localCache = cache;
        if (cache instanceof MultiLevelCache<Object, Object> multiLevelCache) {
            // 如果是多级缓存实例，则获取本地缓存实例
            localCache = multiLevelCache.getLocalCache();
        }

        // 同步本地缓存数据
        if (broadcastMessage.isInvalidate()) {
            localCache.removeAll(broadcastMessage.getKeys());
            log.info("Invalidate local cache success, cacheName: {}, keys: {}", broadcastMessage.getCacheName(), broadcastMessage.getKeys());
        } else if (broadcastMessage.isUpdate()) {
            localCache.putAll(broadcastMessage.getKeyValues());
            log.info("Update local cache success, cacheName: {}, keyValues: {}", broadcastMessage.getCacheName(), broadcastMessage.getKeyValues());
        }
    }

}
