package cn.floseek.fastcache.core.bloomfilter;

/**
 * 布隆过滤器工厂接口
 *
 * @author ChenHongwei472
 */
public interface BloomFilterFactory {

    /**
     * 创建布隆过滤器实例
     *
     * @param bloomFilterConfig 配置信息
     * @return 布隆过滤器实例
     */
    BloomFilter createBloomFilter(BloomFilterConfig bloomFilterConfig);

    /**
     * 获取类型
     *
     * @return 布隆过滤器类型枚举
     */
    BloomFilterType getType();
}
