package cn.floseek.fastcache.cache.serialize;

import cn.floseek.fastcache.cache.serialize.impl.JavaSerializer;
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
    JAVA("java", new JavaSerializer());

    /**
     * 名称
     */
    private final String name;

    /**
     * 序列化器
     */
    private final Serializer serializer;
}
