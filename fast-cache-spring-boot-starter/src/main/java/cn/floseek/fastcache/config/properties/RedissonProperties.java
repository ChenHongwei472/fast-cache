package cn.floseek.fastcache.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 配置属性类
 *
 * @author ChenHongwei472
 */
@Data
@ConfigurationProperties(prefix = RedissonProperties.PREFIX)
public class RedissonProperties {

    public static final String PREFIX = "redisson";

    /**
     * Key 前缀
     */
    private String keyPrefix;
}
