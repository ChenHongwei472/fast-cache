package cn.floseek.fastcache.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class CacheConfig {

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

    /**
     * 是否同步本地缓存
     */
    @Builder.Default
    private boolean syncLocalCache = false;

    /**
     * 分布式缓存过期时间
     */
    private Duration expireTime;
}
