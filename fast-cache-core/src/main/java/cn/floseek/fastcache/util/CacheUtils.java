package cn.floseek.fastcache.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;

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
        if (ArrayUtil.isNotEmpty(params)) {
            for (Object param : params) {
                if (ObjUtil.isNotNull(param)) {
                    sb.append(":").append(param);
                }
            }
        }
        return sb.toString();
    }
}
