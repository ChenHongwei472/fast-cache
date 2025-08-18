package cn.floseek.fastcache.bloomfilter.config;

/**
 * 布隆过滤器类型枚举
 *
 * @author ChenHongwei472
 */
public enum BloomFilterType {
    /**
     * Guava
     */
    GUAVA,
    /**
     * Redisson
     */
    REDISSON
}