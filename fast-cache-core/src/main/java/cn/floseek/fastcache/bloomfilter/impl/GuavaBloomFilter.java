package cn.floseek.fastcache.bloomfilter.impl;

import cn.floseek.fastcache.bloomfilter.BloomFilter;
import cn.floseek.fastcache.bloomfilter.BloomFilterConfig;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * Guava 布隆过滤器
 *
 * @author ChenHongwei472
 */
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class GuavaBloomFilter implements BloomFilter {

    private final BloomFilterConfig bloomFilterConfig;
    private com.google.common.hash.BloomFilter<String> bloomFilter;

    public GuavaBloomFilter(BloomFilterConfig bloomFilterConfig) {
        this.bloomFilterConfig = bloomFilterConfig;
        this.bloomFilter = this.createBloomFilter();
    }

    @Override
    public boolean add(String object) {
        if (ObjectUtils.isEmpty(object)) {
            return false;
        }
        return bloomFilter.put(object);
    }

    @Override
    public long add(Collection<String> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return 0;
        }

        long count = 0;
        for (String element : elements) {
            boolean result = bloomFilter.put(element);
            if (result) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean mightContain(String object) {
        if (ObjectUtils.isEmpty(object)) {
            return false;
        }
        return bloomFilter.mightContain(object);
    }

    @Override
    public void rebuild(List<String> dataList) {
        log.info("开始重建布隆过滤器，key：{}", bloomFilterConfig.getKey());
        com.google.common.hash.BloomFilter<String> backupBloomFilter = this.createBloomFilter();
        if (CollectionUtils.isNotEmpty(dataList)) {
            for (String data : dataList) {
                backupBloomFilter.put(data);
            }
        }
        this.bloomFilter = backupBloomFilter;
        log.info("重建布隆过滤器完成，key：{}，数据量：{}", bloomFilterConfig.getKey(), dataList.size());
    }

    /**
     * 创建布隆过滤器
     *
     * @return 布隆过滤器
     */
    private com.google.common.hash.BloomFilter<String> createBloomFilter() {
        return com.google.common.hash.BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()),
                bloomFilterConfig.getExpectedInsertions(),
                bloomFilterConfig.getFalsePositiveProbability()
        );
    }
}
