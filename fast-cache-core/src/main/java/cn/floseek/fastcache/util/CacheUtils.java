package cn.floseek.fastcache.util;

import cn.floseek.fastcache.constant.CacheConstant;
import org.apache.commons.lang3.StringUtils;

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
        return StringUtils.join(params, CacheConstant.COLON);
    }
}
