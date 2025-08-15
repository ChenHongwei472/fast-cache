package cn.floseek.fastcache.bloomfilter;

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