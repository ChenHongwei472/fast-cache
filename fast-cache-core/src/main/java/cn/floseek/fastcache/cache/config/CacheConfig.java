package cn.floseek.fastcache.cache.config;

import cn.floseek.fastcache.cache.broadcast.BroadcastManager;
import cn.floseek.fastcache.lock.LockTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * 缓存配置类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfig<K, V> implements Serializable {

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
     * 是否同步缓存
     */
    @Builder.Default
    private boolean syncCache = false;

    /**
     * 缓存刷新策略
     */
    private RefreshPolicy refreshPolicy;

    /**
     * 缓存加载器
     */
    private CacheLoader<K, V> loader;

    /**
     * 广播管理器
     */
    private BroadcastManager broadcastManager;

    /**
     * 分布式锁模板
     */
    private LockTemplate lockTemplate;

    // region 本地缓存配置

    /**
     * 本地缓存最大容量
     */
    private Long maximumSize;

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

    /**
     * 是否启用缓存加载器
     *
     * @return boolean
     */
    public boolean loaderEnabled() {
        return Objects.nonNull(this.loader);
    }
}
