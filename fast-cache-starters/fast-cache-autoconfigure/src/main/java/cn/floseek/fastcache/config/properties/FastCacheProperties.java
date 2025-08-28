package cn.floseek.fastcache.config.properties;

import cn.floseek.fastcache.config.GlobalProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FastCache 配置属性
 *
 * @author ChenHongwei472
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = FastCacheProperties.PREFIX)
public class FastCacheProperties extends GlobalProperties {

    public static final String PREFIX = "fast-cache";
}
