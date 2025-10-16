package cn.floseek.fastcache.common.enums;

import java.time.Duration;

/**
 * 缓存键枚举基础接口
 * <p>
 * 封装了获取缓存键相关信息的方法，如缓存名称、缓存过期时间、本地缓存过期时间等，以及生成缓存键等方法，用于简化缓存配置的创建等操作。
 * 使用时可以自定义一个枚举类实现该接口，并定义 name、expireTime、localExpireTime 等相关属性，
 * 最后在创建缓存配置的时候传入具体的枚举项即可，框架会自动获取相关信息填入到缓存配置中。
 * </p>
 *
 * @author ChenHongwei472
 */
public interface BaseCacheKeyEnum {

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String getName();

    /**
     * 获取缓存过期时间
     *
     * @return 缓存过期时间
     */
    Duration getExpireTime();

    /**
     * 获取本地缓存过期时间
     *
     * @return 本地缓存过期时间
     */
    Duration getLocalExpireTime();

}
