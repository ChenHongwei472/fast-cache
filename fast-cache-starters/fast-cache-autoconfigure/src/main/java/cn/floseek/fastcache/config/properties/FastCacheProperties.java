package cn.floseek.fastcache.config.properties;

import cn.floseek.fastcache.config.GlobalProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FastCache 配置属性
 *
 * @author ChenHongwei472
 */
@ConfigurationProperties(prefix = FastCacheProperties.PREFIX)
public class FastCacheProperties extends GlobalProperties {

    public static final String PREFIX = "fast-cache";

}
