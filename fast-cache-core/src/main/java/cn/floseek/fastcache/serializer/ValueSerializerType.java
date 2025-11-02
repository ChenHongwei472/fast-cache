package cn.floseek.fastcache.serializer;

import cn.floseek.fastcache.serializer.impl.JacksonValueSerializer;
import cn.floseek.fastcache.serializer.impl.JavaValueSerializer;

/**
 * 值序列化器类型枚举
 *
 * @author ChenHongwei472
 */
public enum ValueSerializerType {

    /**
     * Java 值序列化器
     */
    JAVA("java", new JavaValueSerializer()),
    /**
     * Jackson 值序列化器
     */
    JACKSON("jackson", new JacksonValueSerializer());

    /**
     * 名称
     */
    private final String name;

    /**
     * 值序列化器实例
     */
    private final ValueSerializer instance;

    ValueSerializerType(String name, ValueSerializer instance) {
        this.name = name;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public ValueSerializer getInstance() {
        return instance;
    }

}
