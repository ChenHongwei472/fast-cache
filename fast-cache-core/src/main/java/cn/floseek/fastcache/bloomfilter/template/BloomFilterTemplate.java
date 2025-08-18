package cn.floseek.fastcache.bloomfilter.template;

import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.config.BloomFilterType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器模板
 *
 * @author ChenHongwei472
 */
public class BloomFilterTemplate {

    /**
     * 布隆过滤器工厂映射
     */
    private final Map<BloomFilterType, BloomFilterFactory> bloomFilterFactoryMap = new ConcurrentHashMap<>();

    /**
     * 注册布隆过滤器工厂
     *
     * @param type    布隆过滤器类型
     * @param factory 布隆过滤器工厂
     */
    public void register(BloomFilterType type, BloomFilterFactory factory) {
        bloomFilterFactoryMap.put(type, factory);
    }

    /**
     * 获取布隆过滤器工厂
     *
     * @param type 布隆过滤器类型
     * @return 布隆过滤器工厂
     */
    public BloomFilterFactory get(BloomFilterType type) {
        return bloomFilterFactoryMap.get(type);
    }
}
