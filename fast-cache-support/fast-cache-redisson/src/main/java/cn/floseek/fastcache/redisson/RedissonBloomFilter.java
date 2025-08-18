package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.bloomfilter.BloomFilter;
import cn.floseek.fastcache.bloomfilter.BloomFilterConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.List;

/**
 * Redisson 布隆过滤器
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonBloomFilter implements BloomFilter {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;
    /**
     * 布隆过滤器配置
     */
    private final BloomFilterConfig bloomFilterConfig;
    /**
     * 布隆过滤器
     */
    private RBloomFilter<String> bloomFilter;

    public RedissonBloomFilter(RedissonClient redissonClient, BloomFilterConfig bloomFilterConfig) {
        this.redissonClient = redissonClient;
        this.bloomFilterConfig = bloomFilterConfig;
        this.bloomFilter = this.getBloomFilter(bloomFilterConfig.getKey());
    }

    @Override
    public boolean add(String object) {
        if (ObjectUtils.isEmpty(object)) {
            return false;
        }
        return bloomFilter.add(object);
    }

    @Override
    public long add(Collection<String> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return 0;
        }
        return bloomFilter.add(elements);
    }

    @Override
    public boolean mightContain(String object) {
        if (ObjectUtils.isEmpty(object)) {
            return false;
        }
        return bloomFilter.contains(object);
    }

    @Override
    public void rebuild(List<String> dataList) {
        String key = bloomFilterConfig.getKey();
        log.info("开始重建布隆过滤器，key：{}", key);
        // 创建备份布隆过滤器
        String backupKey = key + "_backup";
        RBloomFilter<String> backupBloomFilter = this.getBloomFilter(backupKey);
        if (CollectionUtils.isNotEmpty(dataList)) {
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
        if (ObjectUtils.isNotEmpty(bloomFilter) && !bloomFilter.isExists()) {
            bloomFilter.tryInit(bloomFilterConfig.getExpectedInsertions(), bloomFilterConfig.getFalsePositiveProbability());
        }
        return bloomFilter;
    }
}
