package cn.floseek.fastcache.common;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 缓存键基础接口
 * <p>
 * 封装了获取缓存键相关信息的方法，如缓存名称、缓存过期时间、本地缓存过期时间等，以及生成缓存键等方法，用于简化缓存配置的创建等操作。
 * 使用时可以自定义一个枚举类实现该接口，并定义 name、expireTimeMillis、localExpireTimeMillis 等相关属性，
 * 最后在创建缓存配置的时候传入具体的枚举项即可，框架会自动获取相关信息填入到缓存配置中。
 * </p>
 *
 * @author ChenHongwei472
 */
public interface BaseCacheKey {

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String getName();

    /**
     * 获取缓存过期时间（毫秒）
     *
     * @return 缓存过期时间（毫秒）
     */
    Long getExpireTimeMillis();

    /**
     * 获取缓存过期时间
     *
     * @return 缓存过期时间
     */
    default Duration getExpireTime() {
        Long expireTimeMillis = this.getExpireTimeMillis();
        if (Objects.isNull(expireTimeMillis)) {
            expireTimeMillis = CacheConstant.NEVER_EXPIRE;
        }
        return Duration.ofMillis(expireTimeMillis);
    }

    /**
     * 获取本地缓存过期时间（毫秒）
     *
     * @return 本地缓存过期时间（毫秒）
     */
    Long getLocalExpireTimeMillis();

    /**
     * 获取本地缓存过期时间
     *
     * @return 本地缓存过期时间
     */
    default Duration getLocalExpireTime() {
        Long localExpireTimeMillis = this.getLocalExpireTimeMillis();
        if (Objects.isNull(localExpireTimeMillis)) {
            localExpireTimeMillis = CacheConstant.NEVER_EXPIRE;
        }
        return Duration.ofMillis(localExpireTimeMillis);
    }

    /**
     * 生成缓存键
     *
     * @param params 参数
     * @return 缓存键
     */
    default String generateKey(Object... params) {
        StringJoiner joiner = new StringJoiner(CacheConstant.COLON);
        joiner.add(this.getName());
        for (Object param : params) {
            if (Objects.nonNull(param)) {
                joiner.add(String.valueOf(param));
            }
        }
        return joiner.toString();
    }
}
