package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.bloomfilter.AbstractBloomFilter;
import cn.floseek.fastcache.bloomfilter.config.BloomFilterConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.List;

/**
 * 基于 Redisson 的布隆过滤器实现
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonBloomFilter extends AbstractBloomFilter {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 布隆过滤器
     */
    private RBloomFilter<String> bloomFilter;

    public RedissonBloomFilter(RedissonClient redissonClient, BloomFilterConfig config) {
        super(config);
        this.redissonClient = redissonClient;
        this.bloomFilter = this.getBloomFilter(config.getKey());
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
        String key = config.getKey();
        log.info("Start rebuild bloom filter, key: {}", config.getKey());

        // 创建备份布隆过滤器
        String backupKey = key + "_backup";
        RBloomFilter<String> backupBloomFilter = this.getBloomFilter(backupKey);
        if (CollectionUtils.isNotEmpty(dataList)) {
            backupBloomFilter.add(dataList);
        }
        log.info("Create backup bloom filter success, key: {}, data size: {}", backupKey, dataList.size());

        // 切换布隆过滤器
        redissonClient.getKeys().rename(backupKey, key);
        this.bloomFilter = this.getBloomFilter(key);
        log.info("Rebuild bloom filter complete, key: {}, data size: {}", key, dataList.size());
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
            bloomFilter.tryInit(config.getExpectedInsertions(), config.getFalsePositiveProbability());
        }
        return bloomFilter;
    }
}
