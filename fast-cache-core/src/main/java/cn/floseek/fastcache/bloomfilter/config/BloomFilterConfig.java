package cn.floseek.fastcache.bloomfilter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 布隆过滤器配置类
 *
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloomFilterConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 键名
     */
    private String key;

    /**
     * 预计插入元素数量
     */
    private Long expectedInsertions;

    /**
     * 期望误差率
     */
    private Double falsePositiveProbability;
}
