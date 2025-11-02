package cn.floseek.fastcache.test.cache.serialize;

import cn.floseek.fastcache.serializer.ValueSerializer;
import cn.floseek.fastcache.serializer.impl.JacksonValueSerializer;
import cn.floseek.fastcache.serializer.impl.JavaValueSerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 值序列化器测试
 *
 * @author ChenHongwei472
 */
public class ValueSerializerTest {

    private static final Logger log = LoggerFactory.getLogger(ValueSerializerTest.class);

    private final ValueSerializer javaValueSerializer = new JavaValueSerializer();

    private final ValueSerializer jacksonValueSerializer = new JacksonValueSerializer();

    @Test
    public void test_JavaSerializer() {
        byte[] serializeNull = javaValueSerializer.serialize(null);
        log.info("Java serialize null: {}", serializeNull);
        byte[] serializeEmptyList = javaValueSerializer.serialize(new ArrayList<>());
        log.info("Java serialize empty list: {}", serializeEmptyList);
        byte[] serializeEmptyMap = javaValueSerializer.serialize(new HashMap<>());
        log.info("Java serialize empty map: {}", serializeEmptyMap);
        byte[] serializeString = javaValueSerializer.serialize("test");
        log.info("Java serialize string: {}", serializeString);

        Object deserialize = javaValueSerializer.deserialize(serializeNull);
        log.info("Java deserialize null: {}", deserialize);
        deserialize = javaValueSerializer.deserialize(serializeEmptyList);
        log.info("Java deserialize empty list: {}", deserialize);
        deserialize = javaValueSerializer.deserialize(serializeEmptyMap);
        log.info("Java deserialize empty map: {}", deserialize);
        deserialize = javaValueSerializer.deserialize(serializeString);
        log.info("Java deserialize string: {}", deserialize);
    }

    @Test
    public void test_JacksonSerializer() {
        byte[] serializeNull = jacksonValueSerializer.serialize(null);
        log.info("Jackson serialize null: {}", serializeNull);
        byte[] serializeEmptyList = jacksonValueSerializer.serialize(new ArrayList<>());
        log.info("Jackson serialize empty list: {}", serializeEmptyList);
        byte[] serializeEmptyMap = jacksonValueSerializer.serialize(new HashMap<>());
        log.info("Jackson serialize empty map: {}", serializeEmptyMap);
        byte[] serializeString = jacksonValueSerializer.serialize("test");
        log.info("Jackson serialize string: {}", serializeString);

        Object deserialize = jacksonValueSerializer.deserialize(serializeNull);
        log.info("Jackson deserialize null: {}", deserialize);
        deserialize = jacksonValueSerializer.deserialize(serializeEmptyList);
        log.info("Jackson deserialize empty list: {}", deserialize);
        deserialize = jacksonValueSerializer.deserialize(serializeEmptyMap);
        log.info("Jackson deserialize empty map: {}", deserialize);
        deserialize = jacksonValueSerializer.deserialize(serializeString);
        log.info("Jackson deserialize string: {}", deserialize);
    }

}
