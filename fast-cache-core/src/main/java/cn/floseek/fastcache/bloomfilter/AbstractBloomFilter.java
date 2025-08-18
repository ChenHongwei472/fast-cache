package cn.floseek.fastcache.bloomfilter;

import cn.floseek.fastcache.bloomfilter.config.BloomFilterConfig;

/**
 * 布隆过滤器抽象类
 *
 * @author ChenHongwei472
 */
public abstract class AbstractBloomFilter implements BloomFilter {

    /**
     * 布隆过滤器配置
     */
    protected BloomFilterConfig config;

    public AbstractBloomFilter(BloomFilterConfig config) {
        this.config = config;
    }
}
