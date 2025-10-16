package cn.floseek.fastcache.serializer;

import cn.floseek.fastcache.serializer.impl.JacksonValueSerializer;
import cn.floseek.fastcache.serializer.impl.JavaValueSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 值序列化器类型枚举
 *
 * @author ChenHongwei472
 */
@Getter
@RequiredArgsConstructor
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

}
