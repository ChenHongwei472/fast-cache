package cn.floseek.fastcache.model;

import lombok.Data;

/**
 * 布隆过滤器配置类
 *
 * @author ChenHongwei472
 */
@Data
public class BloomFilterConfig {

    /**
     * 键名
     */
    private String key;

    /**
     * 预计插入元素数量
     */
    private long expectedInsertions = 1000000;

    /**
     * 期望误差率
     */
    private double falsePositiveProbability = 0.01;
}
