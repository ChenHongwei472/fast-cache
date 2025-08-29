package cn.floseek.fastcache.cache.serializer.impl;

import cn.floseek.fastcache.cache.serializer.Serializer;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * Java 序列化器
 *
 * @author ChenHongwei472
 */
public class JavaSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) {
        if (Objects.isNull(object)) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Java serializer serialize error: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Java serializer deserialize error: " + e.getMessage(), e);
        }
    }
}
