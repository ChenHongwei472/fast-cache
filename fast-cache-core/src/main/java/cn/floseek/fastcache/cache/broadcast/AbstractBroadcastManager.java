package cn.floseek.fastcache.cache.broadcast;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.config.CacheType;
import cn.floseek.fastcache.cache.decorator.BroadcastDecorator;
import cn.floseek.fastcache.cache.decorator.CacheDecorator;
import cn.floseek.fastcache.cache.impl.multi.MultiLevelCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.UUID;

/**
 * 广播管理器抽象类
 *
 * @author ChenHongwei472
 */
@Slf4j
public abstract class AbstractBroadcastManager implements BroadcastManager {

    /**
     * 实例 ID
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
     * 处理缓存消息
     *
     * @param cacheMessage 缓存消息
     */
    protected void processMessage(CacheMessage cacheMessage) {
        if (ObjectUtils.isEmpty(cacheMessage)) {
            log.info("接收到的缓存消息为空");
            return;
        }

        if (this.instanceId.equals(cacheMessage.getInstanceId())) {
            log.info("缓存消息来自本实例：{}，目标实例：{}，已忽略该消息", this.instanceId, cacheMessage.getInstanceId());
            return;
        }

        CacheDecorator<Object, Object> cacheDecorator = null;
        for (CacheType value : CacheType.values()) {
            Cache<Object, Object> cache = cacheManager.getCache(value, cacheMessage.getCacheName());
            if (cache instanceof CacheDecorator<Object, Object>) {
                cacheDecorator = (CacheDecorator<Object, Object>) cache;
            }
        }

        if (cacheDecorator == null) {
            log.info("缓存实例不存在：{}，已忽略该消息", cacheMessage.getCacheName());
            return;
        }

        if (!cacheDecorator.containsDecorator(BroadcastDecorator.class)) {
            log.info("缓存实例：{}，未添加广播装饰器，已忽略该消息", cacheMessage.getCacheName());
            return;
        }

        Cache<Object, Object> cache = cacheDecorator.unwrapAll();
        if (cache.getCacheType() == CacheType.REMOTE) {
            log.info("缓存实例：{}，为远程缓存实例，已忽略该消息", cacheMessage.getCacheName());
            return;
        }

        Cache<Object, Object> localCache = cache;
        if (cache instanceof MultiLevelCache<Object, Object> multiLevelCache) {
            localCache = multiLevelCache.getLocalCache();
        }
        localCache.removeAll(cacheMessage.getKeys());
        log.info("已删除本地缓存：{}", cacheMessage.getKeys());
    }
}
