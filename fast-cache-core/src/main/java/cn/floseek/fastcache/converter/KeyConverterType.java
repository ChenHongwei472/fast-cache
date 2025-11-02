package cn.floseek.fastcache.converter;

import cn.floseek.fastcache.converter.impl.JacksonKeyConverter;

/**
 * 键名转换器类型枚举
 *
 * @author ChenHongwei472
 */
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

    KeyConverterType(String name, KeyConverter instance) {
        this.name = name;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public KeyConverter getInstance() {
        return instance;
    }

}
