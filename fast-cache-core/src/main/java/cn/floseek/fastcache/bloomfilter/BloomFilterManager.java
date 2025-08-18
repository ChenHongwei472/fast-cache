package cn.floseek.fastcache.bloomfilter;

import cn.floseek.fastcache.bloomfilter.config.BloomFilterConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器管理器
 * <p>
 * 负责对布隆过滤器进行统一的创建和管理
 * </p>
 *
 * @author ChenHongwei472
 */
@Slf4j
public class BloomFilterManager {

    /**
     * 布隆过滤器工厂
     */
    private final BloomFilterFactory factory;

    /**
     * 默认预计插入元素数量
     */
    private final long defaultExpectedInsertions;

    /**
     * 默认期望误差率
     */
    private final double defaultFalsePositiveProbability;

    /**
     * 布隆过滤器映射，key：键名，value：布隆过滤器
     */
    private final Map<String, BloomFilter> bloomFilterMap = new ConcurrentHashMap<>();

    public BloomFilterManager(BloomFilterFactory factory, long defaultExpectedInsertions, double defaultFalsePositiveProbability) {
        this.factory = factory;
        this.defaultExpectedInsertions = defaultExpectedInsertions;
        this.defaultFalsePositiveProbability = defaultFalsePositiveProbability;
    }

    /**
     * 获取或创建布隆过滤器
     *
     * @param bloomFilterConfig 布隆过滤器配置对象
     * @return 布隆过滤器
     */
    public BloomFilter getOrCreateBloomFilter(BloomFilterConfig bloomFilterConfig) {
        // 设置默认值
        if (bloomFilterConfig.getExpectedInsertions() == null) {
            bloomFilterConfig.setExpectedInsertions(defaultExpectedInsertions);
        }
        if (bloomFilterConfig.getFalsePositiveProbability() == null) {
            bloomFilterConfig.setFalsePositiveProbability(defaultFalsePositiveProbability);
        }

        return bloomFilterMap.computeIfAbsent(bloomFilterConfig.getKey(),
                k -> factory.createBloomFilter(bloomFilterConfig));
    }

    /**
     * 获取布隆过滤器
     *
     * @param key 键名
     * @return 布隆过滤器
     */
    public BloomFilter getBloomFilter(String key) {
        return bloomFilterMap.get(key);
    }
}
