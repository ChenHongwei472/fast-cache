package cn.floseek.fastcache.cache.converter.impl;

import cn.floseek.fastcache.cache.converter.KeyConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Jackson 键名转换器
 *
 * @author ChenHongwei472
 */
public class JacksonKeyConverter implements KeyConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convert(Object originalKey) {
        if (Objects.isNull(originalKey)) {
            return null;
        }

        if (originalKey instanceof String string) {
            return string;
        } else if (originalKey instanceof Character character) {
            return character.toString();
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(originalKey);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Jackson key convert error", e);
        }
    }
}
