package cn.floseek.fastcache.manager.broadcast;

import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.model.CacheMessage;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 检查配置属性
     *
     * @param fastCacheProperties 配置属性
     */
    protected void checkProperties(FastCacheProperties fastCacheProperties) {
        if (StrUtil.isBlank(fastCacheProperties.getBroadcastChannel())) {
            throw new IllegalArgumentException("广播通道不能为空");
        }
    }

    /**
     * 处理缓存消息
     *
     * @param cacheMessage 缓存消息
     */
    protected void processMessage(CacheMessage cacheMessage) {
        if (ObjUtil.isNull(cacheMessage)) {
            log.info("接收到的缓存消息为空");
            return;
        }

        if (this.instanceId.equals(cacheMessage.getInstanceId())) {
            log.info("缓存消息来自本实例：{}，目标实例：{}，已忽略该消息", this.instanceId, cacheMessage.getInstanceId());
            return;
        }

        Cache<Object, Object> localCache = LocalCacheManager.getInstance().getCache(cacheMessage.getCacheName());
        if (localCache == null) {
            log.info("本地缓存实例不存在：{}，已忽略该消息", cacheMessage.getCacheName());
            return;
        }

        localCache.invalidateAll(cacheMessage.getKeys());
    }
}
