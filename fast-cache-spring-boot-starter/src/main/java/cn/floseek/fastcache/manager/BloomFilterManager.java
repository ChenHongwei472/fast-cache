package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.model.BloomFilterConfig;
import cn.floseek.fastcache.service.bloomfilter.BloomFilter;
import cn.floseek.fastcache.service.bloomfilter.impl.RedissonBloomFilter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器管理器
 *
 * @author ChenHongwei472
 */
@RequiredArgsConstructor
public class BloomFilterManager {

    private final RedissonClient redissonClient;

    /**
     * 布隆过滤器映射，key：键名，value：布隆过滤器实例
     */
    private final Map<String, BloomFilter> bloomFilterMap = new ConcurrentHashMap<>();

    /**
     * 获取或创建布隆过滤器实例
     *
     * @param bloomFilterConfig 布隆过滤器配置
     * @return 布隆过滤器实例
     */
    public BloomFilter getOrCreateBloomFilter(BloomFilterConfig bloomFilterConfig) {
        String key = bloomFilterConfig.getKey();
        if (bloomFilterMap.containsKey(key)) {
            return bloomFilterMap.get(key);
        }

        BloomFilter bloomFilter = new RedissonBloomFilter(redissonClient, bloomFilterConfig);
        bloomFilterMap.put(key, bloomFilter);
        return bloomFilter;
    }

    /**
     * 获取布隆过滤器实例
     *
     * @param key 键名
     * @return 布隆过滤器实例
     */
    public BloomFilter getBloomFilter(String key) {
        return bloomFilterMap.get(key);
    }
}
