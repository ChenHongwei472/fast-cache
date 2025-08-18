package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.broadcast.AbstractBroadcastManager;
import cn.floseek.fastcache.cache.broadcast.BroadcastMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 Redisson 的广播管理器实现
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonBroadcastManager extends AbstractBroadcastManager {

    /**
     * 未订阅状态标识
     */
    private static final int UNSUBSCRIBED = 0;

    /**
     * 订阅 ID，0 表示未订阅状态
     */
    private volatile int subscribeId = UNSUBSCRIBED;

    /**
     * 订阅锁
     */
    private final ReentrantLock reentrantLock = new ReentrantLock();

    private final String channel;
    private final RedissonClient redissonClient;

    public RedissonBroadcastManager(CacheManager cacheManager, RedissonClient redissonClient) {
        super(cacheManager);
        this.channel = cacheManager.getGlobalProperties().getRemote().getBroadcastChannel();
        this.redissonClient = redissonClient;
    }

    @Override
    public void publish(BroadcastMessage broadcastMessage) {
        try {
            if (StringUtils.isNotBlank(this.channel) && ObjectUtils.isNotEmpty(broadcastMessage)) {
                this.redissonClient.getTopic(this.channel).publish(broadcastMessage);
                log.debug("Broadcast message published successfully, channel: {}", this.channel);
            }
        } catch (Throwable e) {
            log.error("Failed to publish broadcast message, channel: {}", this.channel, e);
        }
    }

    @Override
    public void subscribe() {
        reentrantLock.lock();
        try {
            if (this.subscribeId == UNSUBSCRIBED && StringUtils.isNotBlank(this.channel)) {
                this.subscribeId = this.redissonClient.getTopic(this.channel)
                        .addListener(BroadcastMessage.class, (channel, message) -> processMessage(message));
                log.info("Subscribed to broadcast channel: {}", this.channel);
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void close() {
        reentrantLock.lock();
        try {
            if (this.subscribeId != UNSUBSCRIBED && StringUtils.isNotBlank(this.channel)) {
                try {
                    this.redissonClient.getTopic(this.channel).removeListener(this.subscribeId);
                    log.info("Unsubscribed from broadcast channel: {}", this.channel);
                } catch (Throwable e) {
                    log.error("Failed to unsubscribe from broadcast channel: {}", this.channel, e);
                } finally {
                    this.subscribeId = UNSUBSCRIBED;
                }
            }
        } finally {
            reentrantLock.unlock();
        }
    }
}
