package cn.floseek.fastcache.service.bloomfilter.impl;

import cn.floseek.fastcache.model.BloomFilterConfig;
import cn.floseek.fastcache.service.bloomfilter.BloomFilterService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.List;

/**
 * Redisson 布隆过滤器服务实现
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonBloomFilterService implements BloomFilterService {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 键名
     */
    private final String key;

    /**
     * 预计插入元素数量
     */
    private final long expectedInsertions;

    /**
     * 期望误差率
     */
    private final double falsePositiveProbability;

    /**
     * 布隆过滤器
     */
    private RBloomFilter<String> bloomFilter;

    public RedissonBloomFilterService(RedissonClient redissonClient, BloomFilterConfig bloomFilterConfig) {
        this.redissonClient = redissonClient;
        this.key = bloomFilterConfig.getKey();
        this.expectedInsertions = bloomFilterConfig.getExpectedInsertions();
        this.falsePositiveProbability = bloomFilterConfig.getFalsePositiveProbability();

        this.bloomFilter = this.getBloomFilter(key);
    }

    @Override
    public boolean add(String object) {
        if (ObjUtil.isNull(object)) {
            return false;
        }
        return bloomFilter.add(object);
    }

    @Override
    public long add(Collection<String> elements) {
        if (CollUtil.isEmpty(elements)) {
            return 0;
        }
        return bloomFilter.add(elements);
    }

    @Override
    public boolean mightContain(String object) {
        if (ObjUtil.isNull(object)) {
            return false;
        }
        return bloomFilter.contains(object);
    }

    @Override
    public void rebuild(List<String> dataList) {
        log.info("开始重建布隆过滤器，key：{}", key);
        // 创建备份布隆过滤器
        String backupKey = key + "_backup";
        RBloomFilter<String> backupBloomFilter = this.getBloomFilter(backupKey);
        if (CollUtil.isNotEmpty(dataList)) {
            backupBloomFilter.add(dataList);
        }
        log.info("创建备份布隆过滤器成功，key：{}，数据量：{}", backupKey, dataList.size());
        // 切换布隆过滤器
        redissonClient.getKeys().rename(backupKey, key);
        this.bloomFilter = this.getBloomFilter(key);
        log.info("重建布隆过滤器完成，key：{}", key);
    }

    /**
     * 获取布隆过滤器
     *
     * @param key 键名
     * @return 布隆过滤器
     */
    private RBloomFilter<String> getBloomFilter(String key) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(key);
        if (ObjUtil.isNotNull(bloomFilter) && !bloomFilter.isExists()) {
            bloomFilter.tryInit(expectedInsertions, falsePositiveProbability);
        }
        return bloomFilter;
    }
}
