package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.bloomfilter.BloomFilter;
import cn.floseek.fastcache.bloomfilter.BloomFilterConfig;
import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.BloomFilterType;
import org.redisson.api.RedissonClient;

/**
 * Redisson 布隆过滤器工厂实现
 *
 * @author ChenHongwei472
 */
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
