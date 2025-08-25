package cn.floseek.fastcache.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁模板接口
 * <p>
 * 提供通用的分布式锁操作能力，屏蔽底层实现差异，以统一的方式操作锁
 * </p>
 *
 * @author ChenHongwei472
 */
public interface LockTemplate {

    /**
     * 尝试获取锁
     *
     * @param name      锁名称
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     * @param timeUnit  时间单位
     * @return boolean
     * @throws InterruptedException 如果线程被中断
     */
    boolean tryLock(String name, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 获取锁
     *
     * @param name      锁名称
     * @param leaseTime 租约时间
     * @param timeUnit  时间单位
     */
    void lock(String name, long leaseTime, TimeUnit timeUnit);

    /**
     * 释放锁
     *
     * @param name 锁名称
     */
    void unlock(String name);
}
