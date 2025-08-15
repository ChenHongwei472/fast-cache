package cn.floseek.fastcache.bloomfilter;

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

    /**
     * 默认预计插入元素数量
     */
    private final long defaultExpectedInsertions;

    /**
     * 默认期望误差率
     */
    private final double defaultFalsePositiveProbability;

    public BloomFilterManager(BloomFilterFactory bloomFilterFactory, long defaultExpectedInsertions, double defaultFalsePositiveProbability) {
        this.bloomFilterFactory = bloomFilterFactory;
        this.defaultExpectedInsertions = defaultExpectedInsertions;
        this.defaultFalsePositiveProbability = defaultFalsePositiveProbability;
    }

    /**
     * 获取或创建布隆过滤器实例
     *
     * @param bloomFilterConfig 布隆过滤器配置
     * @return 布隆过滤器实例
     */
    public BloomFilter getOrCreateBloomFilter(BloomFilterConfig bloomFilterConfig) {
        if (bloomFilterConfig.getExpectedInsertions() == null) {
            bloomFilterConfig.setExpectedInsertions(defaultExpectedInsertions);
        }

        if (bloomFilterConfig.getFalsePositiveProbability() == null) {
            bloomFilterConfig.setFalsePositiveProbability(defaultFalsePositiveProbability);
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
