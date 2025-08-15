package cn.floseek.fastcache.support.redisson;

import cn.floseek.fastcache.manager.broadcast.AbstractBroadcastManager;
import cn.floseek.fastcache.model.CacheMessage;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Redisson 广播管理器
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonBroadcastManager extends AbstractBroadcastManager {

    private final String channel;
    private final RedissonClient redissonClient;
    private volatile int subscribeId;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public RedissonBroadcastManager(String channel, RedissonClient redissonClient) {
        this.channel = channel;
        this.redissonClient = redissonClient;
    }

    @Override
    public void publish(CacheMessage cacheMessage) {
        try {
            if (StrUtil.isNotBlank(this.channel) && ObjUtil.isNotNull(cacheMessage)) {
                this.redissonClient.getTopic(this.channel).publish(cacheMessage);
            }
        } catch (Throwable e) {
            log.error("发布缓存消息失败", e);
        }
    }

    @Override
    public void startSubscribe() {
        reentrantLock.lock();
        try {
            if (this.subscribeId == 0 && StrUtil.isNotBlank(this.channel)) {
                this.subscribeId = this.redissonClient.getTopic(this.channel)
                        .addListener(CacheMessage.class, (channel, message) -> this.processMessage(message));
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void close() {
        reentrantLock.lock();
        try {
            final int id;
            if ((id = this.subscribeId) > 0 && StrUtil.isNotBlank(this.channel)) {
                this.subscribeId = 0;
                try {
                    this.redissonClient.getTopic(this.channel).removeListener(id);
                } catch (Throwable e) {
                    log.error("取消订阅 {} 失败", this.channel, e);
                }
            }
        } finally {
            reentrantLock.unlock();
        }
    }
}
