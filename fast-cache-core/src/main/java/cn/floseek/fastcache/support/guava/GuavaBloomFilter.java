package cn.floseek.fastcache.support.guava;

import cn.floseek.fastcache.core.bloomfilter.BloomFilter;
import cn.floseek.fastcache.core.bloomfilter.BloomFilterConfig;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * Hutool 布隆过滤器
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
        if (ObjUtil.isNull(object)) {
            return false;
        }
        return bloomFilter.put(object);
    }

    @Override
    public long add(Collection<String> elements) {
        if (CollUtil.isEmpty(elements)) {
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
        if (ObjUtil.isNull(object)) {
            return false;
        }
        return bloomFilter.mightContain(object);
    }

    @Override
    public void rebuild(List<String> dataList) {
        log.info("开始重建布隆过滤器，key：{}", bloomFilterConfig.getKey());
        log.info("{}", bloomFilterConfig);
        com.google.common.hash.BloomFilter<String> backupBloomFilter = this.createBloomFilter();
        if (CollUtil.isNotEmpty(dataList)) {
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
