package cn.floseek.fastcache.cache.converter;

/**
 * 键名转换器
 * <p>
 * 用于将任意类型的原始键转换为字符串形式
 * </p>
 *
 * @author ChenHongwei472
 */
public interface KeyConverter {

    /**
     * 转换
     *
     * @param originalKey 原始键
     * @return 转换后的键
     */
    String convert(Object originalKey);
}
