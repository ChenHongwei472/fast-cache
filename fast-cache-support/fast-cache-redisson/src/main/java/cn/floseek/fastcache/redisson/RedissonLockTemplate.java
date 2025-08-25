package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.lock.LockTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式锁模板
 *
 * @author ChenHongwei472
 */
public class RedissonLockTemplate implements LockTemplate {

    private final RedissonClient redissonClient;

    public RedissonLockTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(String name, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = this.getLock(name);
        return lock.tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public void lock(String name, long leaseTime, TimeUnit timeUnit) {
        RLock lock = this.getLock(name);
        lock.lock(leaseTime, timeUnit);
    }

    @Override
    public void unlock(String name) {
        RLock lock = this.getLock(name);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 获取锁对象
     *
     * @param name 锁名称
     * @return 锁对象
     */
    private RLock getLock(String name) {
        return redissonClient.getLock(name);
    }
}

