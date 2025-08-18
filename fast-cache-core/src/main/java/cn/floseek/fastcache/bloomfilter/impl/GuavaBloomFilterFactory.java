package cn.floseek.fastcache.bloomfilter.impl;

import cn.floseek.fastcache.bloomfilter.BloomFilter;
import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.config.BloomFilterConfig;
import cn.floseek.fastcache.bloomfilter.config.BloomFilterType;

/**
 * Guava 布隆过滤器工厂实现
 *
 * @author ChenHongwei472
 */
public class GuavaBloomFilterFactory implements BloomFilterFactory {

    @Override
    public BloomFilter createBloomFilter(BloomFilterConfig bloomFilterConfig) {
        return new GuavaBloomFilter(bloomFilterConfig);
    }

    @Override
    public BloomFilterType getType() {
        return BloomFilterType.GUAVA;
    }
}
