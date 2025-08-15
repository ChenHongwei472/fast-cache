package cn.floseek.fastcache.support.redisson;

import cn.floseek.fastcache.core.bloomfilter.BloomFilter;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterConfig;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Redisson 布隆过滤器工厂实现
 *
 * @author ChenHongwei472
 */
@Component
public class RedissonBloomFilterFactory implements BloomFilterFactory {

    private final RedissonClient redissonClient;

    public RedissonBloomFilterFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public BloomFilter createBloomFilter(BloomFilterConfig bloomFilterConfig) {
        return new RedissonBloomFilter(redissonClient, bloomFilterConfig);
    }

    @Override
    public BloomFilterType getType() {
        return BloomFilterType.REDISSON;
    }
}
