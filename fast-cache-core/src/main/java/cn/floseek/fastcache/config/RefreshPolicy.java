package cn.floseek.fastcache.config;

import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存刷新策略
 *
 * @author ChenHongwei472
 */
@Getter
@ToString
public class RefreshPolicy {

    /**
     * 刷新间隔时间（毫秒）
     */
    private long refreshMillis;

    /**
     * 最后一次访问后停止刷新的时间（毫秒）
     */
    private long stopRefreshAfterLastAccessMillis;

    /**
     * 刷新锁超时时间（毫秒）
     */
    private long refreshLockTimeoutMillis = 60 * 1000;

    /**
     * 创建缓存刷新策略
     *
     * @param duration 刷新间隔时间
     * @return 缓存刷新策略
     */
    public static RefreshPolicy newPolicy(Duration duration) {
        RefreshPolicy refreshPolicy = new RefreshPolicy();
        refreshPolicy.refreshMillis = duration.toMillis();
        return refreshPolicy;
    }

    /**
     * 创建缓存刷新策略
     *
     * @param time     刷新间隔时间
     * @param timeUnit 时间单位
     * @return 缓存刷新策略
     */
    public static RefreshPolicy newPolicy(long time, TimeUnit timeUnit) {
        RefreshPolicy refreshPolicy = new RefreshPolicy();
        refreshPolicy.refreshMillis = timeUnit.toMillis(time);
        return refreshPolicy;
    }

    /**
     * 设置最后一次访问后停止刷新的时间
     *
     * @param duration 最后一次访问后停止刷新的时间
     * @return 缓存刷新策略
     */
    public RefreshPolicy stopRefreshAfterLastAccess(Duration duration) {
        this.stopRefreshAfterLastAccessMillis = duration.toMillis();
        return this;
    }

    /**
     * 设置最后一次访问后停止刷新的时间
     *
     * @param time     最后一次访问后停止刷新的时间
     * @param timeUnit 时间单位
     * @return 缓存刷新策略
     */
    public RefreshPolicy stopRefreshAfterLastAccess(long time, TimeUnit timeUnit) {
        this.stopRefreshAfterLastAccessMillis = timeUnit.toMillis(time);
        return this;
    }

    /**
     * 设置刷新锁超时时间
     *
     * @param duration 刷新锁超时时间
     * @return 缓存刷新策略
     */
    public RefreshPolicy refreshLockTimeout(Duration duration) {
        this.refreshLockTimeoutMillis = duration.toMillis();
        return this;
    }

    /**
     * 创建缓存刷新策略
     *
     * @param time     刷新锁超时时间
     * @param timeUnit 时间单位
     * @return 缓存刷新策略
     */
    public RefreshPolicy refreshLockTimeout(long time, TimeUnit timeUnit) {
        this.refreshLockTimeoutMillis = timeUnit.toMillis(time);
        return this;
    }

}
