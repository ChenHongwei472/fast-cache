package cn.floseek.fastcache.cache.serializer;

import cn.floseek.fastcache.cache.serializer.impl.JacksonSerializer;
import cn.floseek.fastcache.cache.serializer.impl.JavaSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 序列化器类型枚举
 *
 * @author ChenHongwei472
 */
@Getter
@RequiredArgsConstructor
public enum SerializerType {
    /**
     * Java 序列化器
     */
    JAVA("java", new JavaSerializer()),
    /**
     * Jackson 序列化器
     */
    JACKSON("jackson", new JacksonSerializer());

    /**
     * 名称
     */
    private final String name;

    /**
     * 序列化器
     */
    private final Serializer serializer;
}
