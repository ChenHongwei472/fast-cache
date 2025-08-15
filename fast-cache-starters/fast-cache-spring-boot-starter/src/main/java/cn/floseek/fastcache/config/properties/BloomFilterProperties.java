package cn.floseek.fastcache.config.properties;

import cn.floseek.fastcache.core.bloomfilter.BloomFilterType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 布隆过滤器配置属性
 *
 * @author ChenHongwei472
 */
@Data
@ConfigurationProperties(prefix = BloomFilterProperties.PREFIX)
public class BloomFilterProperties {

    public static final String PREFIX = "fast-cache.bloom-filter";

    /**
     * 布隆过滤器类型
     */
    private BloomFilterType type = BloomFilterType.GUAVA;

    /**
     * 预计插入元素数量
     */
    private long expectedInsertions = 1000000L;

    /**
     * 期望误差率
     */
    private double falsePositiveProbability = 0.01;
}
