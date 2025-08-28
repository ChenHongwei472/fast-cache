package cn.floseek.fastcache.util;

import cn.floseek.fastcache.common.CacheConstant;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 缓存工具类
 *
 * @author ChenHongwei472
 */
public class CacheUtils {

    /**
     * 生成缓存键
     *
     * @param params 参数
     * @return 缓存键
     */
    public static String generateKey(Object... params) {
        StringJoiner joiner = new StringJoiner(CacheConstant.COLON);
        for (Object param : params) {
            if (Objects.nonNull(param)) {
                joiner.add(String.valueOf(param));
            }
        }
        return joiner.toString();
    }
}
