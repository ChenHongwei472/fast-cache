package cn.floseek.fastcache.model;

/**
 * 缓存类型枚举
 *
 * @author ChenHongwei472
 */
public enum CacheType {
    /**
     * 本地缓存
     */
    LOCAL,
    /**
     * 分布式缓存
     */
    REMOTE,
    /**
     * 多级缓存
     */
    MULTI_LEVEL
}
