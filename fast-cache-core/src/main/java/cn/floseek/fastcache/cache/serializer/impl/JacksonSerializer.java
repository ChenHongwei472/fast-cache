package cn.floseek.fastcache.cache.serializer.impl;

import cn.floseek.fastcache.cache.serializer.Serializer;
import cn.floseek.fastcache.common.CacheException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.apache.commons.lang3.ArrayUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Jackson 序列化器
 *
 * @author ChenHongwei472
 */
public class JacksonSerializer implements Serializer {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JacksonSerializer() {
        // 忽略未知属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略空对象
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 日期时间处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
        objectMapper.registerModule(javaTimeModule);

        // 添加类型信息
        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL, LaissezFaireSubTypeValidator.instance) {

            @Override
            public boolean useForType(JavaType t) {
                if (t.isPrimitive()) {
                    return false;
                }

                if (_appliesFor == ObjectMapper.DefaultTyping.NON_FINAL) {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // 19-Apr-2016, tatu: ReferenceType like Optional also requires similar handling:
                    while (t.isReferenceType()) {
                        t = t.getReferencedType();
                    }
                    // to fix problem with wrong long to int conversion
                    if (t.getRawClass() == Long.class) {
                        return true;
                    }
                    if (t.getRawClass() == XMLGregorianCalendar.class) {
                        return false;
                    }
                    // [databind#88] Should not apply to JSON tree models:
                    return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
                }
                return super.useForType(t);
            }
        };
        typer.init(JsonTypeInfo.Id.CLASS, null);
        typer.inclusion(JsonTypeInfo.As.PROPERTY);
        objectMapper.setDefaultTyping(typer);
    }

    @Override
    public <T> byte[] serialize(T object) {
        if (Objects.isNull(object)) {
            return new byte[0];
        }

        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new CacheException("Jackson serializer serialize error: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new CacheException("Jackson serializer deserialize error: " + e.getMessage(), e);
        }
    }

}
