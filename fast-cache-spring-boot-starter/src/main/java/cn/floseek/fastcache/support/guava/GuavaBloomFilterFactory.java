package cn.floseek.fastcache.support.guava;

import cn.floseek.fastcache.core.bloomfilter.BloomFilter;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterConfig;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterType;
import org.springframework.stereotype.Component;

/**
 * Guava 布隆过滤器工厂实现
 *
 * @author ChenHongwei472
 */
@Component
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
