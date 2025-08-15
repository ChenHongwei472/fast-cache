package cn.floseek.fastcache.handler;

import cn.hutool.core.util.StrUtil;
import org.redisson.api.NameMapper;

/**
 * Redisson Key 前缀处理器
 *
 * @author ChenHongwei472
 */
public class RedissonKeyPrefixHandler implements NameMapper {

    private final String keyPrefix;

    public RedissonKeyPrefixHandler(String keyPrefix) {
        // 如果前缀为空，则返回空字符串
        this.keyPrefix = StrUtil.isBlank(keyPrefix) ? StrUtil.EMPTY : keyPrefix.concat(StrUtil.COLON);
    }

    @Override
    public String map(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        if (StrUtil.isNotBlank(keyPrefix) && !name.startsWith(keyPrefix)) {
            return keyPrefix.concat(name);
        }
        return name;
    }

    @Override
    public String unmap(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        if (StrUtil.isNotBlank(keyPrefix) && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }
}
