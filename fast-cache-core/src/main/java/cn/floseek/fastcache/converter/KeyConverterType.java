package cn.floseek.fastcache.converter;

import cn.floseek.fastcache.converter.impl.JacksonKeyConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 键名转换器类型枚举
 *
 * @author ChenHongwei472
 */
@Getter
@RequiredArgsConstructor
public enum KeyConverterType {

    /**
     * Jackson 键名转换器
     */
    JACKSON("jackson", new JacksonKeyConverter());

    /**
     * 名称
     */
    private final String name;

    /**
     * 键名转换器实例
     */
    private final KeyConverter instance;

}
