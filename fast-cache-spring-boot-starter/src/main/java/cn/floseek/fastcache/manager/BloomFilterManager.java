package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.config.properties.BloomFilterProperties;
import cn.floseek.fastcache.core.bloomfilter.BloomFilter;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterConfig;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器管理器
 *
 * @author ChenHongwei472
 */
@Slf4j
public class BloomFilterManager {

    /**
     * 布隆过滤器映射，key：键名，value：布隆过滤器实例
     */
    private final Map<String, BloomFilter> bloomFilterMap = new ConcurrentHashMap<>();

    private final BloomFilterFactory bloomFilterFactory;
    private final BloomFilterProperties bloomFilterProperties;

    public BloomFilterManager(BloomFilterFactory bloomFilterFactory, BloomFilterProperties bloomFilterProperties) {
        this.bloomFilterFactory = bloomFilterFactory;
        this.bloomFilterProperties = bloomFilterProperties;
    }

    /**
     * 获取或创建布隆过滤器实例
     *
     * @param bloomFilterConfig 布隆过滤器配置
     * @return 布隆过滤器实例
     */
    public BloomFilter getOrCreateBloomFilter(BloomFilterConfig bloomFilterConfig) {
        if (bloomFilterConfig.getExpectedInsertions() == null) {
            bloomFilterConfig.setExpectedInsertions(bloomFilterProperties.getExpectedInsertions());
        }

        if (bloomFilterConfig.getFalsePositiveProbability() == null) {
            bloomFilterConfig.setFalsePositiveProbability(bloomFilterProperties.getFalsePositiveProbability());
        }

        return bloomFilterMap.computeIfAbsent(bloomFilterConfig.getKey(),
                k -> bloomFilterFactory.createBloomFilter(bloomFilterConfig));
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
