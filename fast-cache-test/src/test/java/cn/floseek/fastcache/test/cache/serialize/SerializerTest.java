package cn.floseek.fastcache.test.cache.serialize;

import cn.floseek.fastcache.serializer.Serializer;
import cn.floseek.fastcache.serializer.impl.JacksonSerializer;
import cn.floseek.fastcache.serializer.impl.JavaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 序列化器测试
 *
 * @author ChenHongwei472
 */
@Slf4j
public class SerializerTest {

    private final Serializer javaSerializer = new JavaSerializer();

    private final Serializer jacksonSerializer = new JacksonSerializer();

    @Test
    public void test_JavaSerializer() {
        byte[] serializeNull = javaSerializer.serialize(null);
        log.info("Java serialize null: {}", serializeNull);
        byte[] serializeEmptyList = javaSerializer.serialize(new ArrayList<>());
        log.info("Java serialize empty list: {}", serializeEmptyList);
        byte[] serializeEmptyMap = javaSerializer.serialize(new HashMap<>());
        log.info("Java serialize empty map: {}", serializeEmptyMap);
        byte[] serializeString = javaSerializer.serialize("test");
        log.info("Java serialize string: {}", serializeString);

        Object deserialize = javaSerializer.deserialize(serializeNull);
        log.info("Java deserialize null: {}", deserialize);
        deserialize = javaSerializer.deserialize(serializeEmptyList);
        log.info("Java deserialize empty list: {}", deserialize);
        deserialize = javaSerializer.deserialize(serializeEmptyMap);
        log.info("Java deserialize empty map: {}", deserialize);
        deserialize = javaSerializer.deserialize(serializeString);
        log.info("Java deserialize string: {}", deserialize);
    }

    @Test
    public void test_JacksonSerializer() {
        byte[] serializeNull = jacksonSerializer.serialize(null);
        log.info("Jackson serialize null: {}", serializeNull);
        byte[] serializeEmptyList = jacksonSerializer.serialize(new ArrayList<>());
        log.info("Jackson serialize empty list: {}", serializeEmptyList);
        byte[] serializeEmptyMap = jacksonSerializer.serialize(new HashMap<>());
        log.info("Jackson serialize empty map: {}", serializeEmptyMap);
        byte[] serializeString = jacksonSerializer.serialize("test");
        log.info("Jackson serialize string: {}", serializeString);

        Object deserialize = jacksonSerializer.deserialize(serializeNull);
        log.info("Jackson deserialize null: {}", deserialize);
        deserialize = jacksonSerializer.deserialize(serializeEmptyList);
        log.info("Jackson deserialize empty list: {}", deserialize);
        deserialize = jacksonSerializer.deserialize(serializeEmptyMap);
        log.info("Jackson deserialize empty map: {}", deserialize);
        deserialize = jacksonSerializer.deserialize(serializeString);
        log.info("Jackson deserialize string: {}", deserialize);
    }

}
