package cn.floseek.fastcache.common;

import java.time.Duration;

/**
 * 缓存常量
 *
 * @author ChenHongwei472
 */
public interface CacheConstant {

    /**
     * 冒号
     */
    String COLON = ":";

    /**
     * 永不过期
     */
    Duration NEVER_EXPIRE = Duration.ofMillis(-1L);

}
