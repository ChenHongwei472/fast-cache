package cn.floseek.fastcache.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FastCache 配置属性
 *
 * @author ChenHongwei472
 */
@Data
@ConfigurationProperties(prefix = FastCacheProperties.PREFIX)
public class FastCacheProperties {

    public static final String PREFIX = "fast-cache";

    /**
     * Key 前缀
     */
    private String keyPrefix;

    /**
     * 广播通道
     */
    private String broadcastChannel = "default_broadcast_channel";
}
