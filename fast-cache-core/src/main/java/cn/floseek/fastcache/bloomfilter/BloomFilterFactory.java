package cn.floseek.fastcache.bloomfilter;

import cn.floseek.fastcache.bloomfilter.config.BloomFilterConfig;
import cn.floseek.fastcache.bloomfilter.config.BloomFilterType;

/**
 * 布隆过滤器工厂接口
 *
 * @author ChenHongwei472
 */
public interface BloomFilterFactory {

    /**
     * 创建布隆过滤器
     *
     * @param bloomFilterConfig 布隆过滤器配置对象
     * @return 布隆过滤器
     */
    BloomFilter createBloomFilter(BloomFilterConfig bloomFilterConfig);

    /**
     * 获取布隆过滤器类型
     *
     * @return 布隆过滤器类型枚举
     */
    BloomFilterType getType();
}
