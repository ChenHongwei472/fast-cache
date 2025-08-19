package cn.floseek.fastcache.cache.config;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

/**
 * 缓存配置类
 *
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存类型
     */
    @Builder.Default
    private CacheType cacheType = CacheType.REMOTE;

    /**
     * 是否同步本地缓存
     */
    @Builder.Default
    private boolean syncLocalCache = false;

    /**
     * 缓存加载器
     */
    private CacheLoader<?, ?> loader;

    /**
     * 广播管理器
     */
    private BroadcastManager broadcastManager;

    // region 本地缓存配置

    /**
     * 本地缓存最大容量
     */
    @Builder.Default
    private long maximumSize = 1000;

    /**
     * 本地缓存写入后过期时间
     */
    private Duration expireAfterWrite;

    /**
     * 本地缓存访问后过期时间
     */
    private Duration expireAfterAccess;

    // endregion

    // region 分布式缓存配置

    /**
     * 分布式缓存过期时间
     */
    private Duration expireTime;

    // endregion
}
