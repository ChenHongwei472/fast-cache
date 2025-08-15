package cn.floseek.fastcache.core.bloomfilter;

/**
 * 布隆过滤器类型
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