package cn.floseek.fastcache.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

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
        StringBuilder sb = new StringBuilder();
        if (ArrayUtils.isNotEmpty(params)) {
            for (Object param : params) {
                if (ObjectUtils.isNotEmpty(param)) {
                    sb.append(":").append(param);
                }
            }
        }
        return sb.toString();
    }
}
