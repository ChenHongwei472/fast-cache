package cn.floseek.fastcache.config;

import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.handler.RedissonKeyPrefixHandler;
import cn.floseek.fastcache.lock.LockTemplate;
import cn.floseek.fastcache.redis.RedisService;
import cn.floseek.fastcache.redisson.RedissonBloomFilterFactory;
import cn.floseek.fastcache.redisson.RedissonLockTemplate;
import cn.floseek.fastcache.redisson.RedissonService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Redisson 自动配置类
 *
 * @author ChenHongwei472
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FastCacheProperties.class)
public class RedissonAutoConfiguration {

    @Bean
    public RedisService redisService(RedissonClient redissonClient) {
        return new RedissonService(redissonClient);
    }

    @Bean
    public BloomFilterFactory redissonBloomFilterFactory(RedissonClient redissonClient) {
        return new RedissonBloomFilterFactory(redissonClient);
    }

    @Bean
    public LockTemplate lockTemplate(RedissonClient redissonClient) {
        return new RedissonLockTemplate(redissonClient);
    }

    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer(FastCacheProperties fastCacheProperties, RedisProperties redisProperties) {
        return config -> {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(new JavaTimeModule());

            TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, objectMapper);
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);
            config.setCodec(codec);

            RedissonKeyPrefixHandler keyPrefixHandler = new RedissonKeyPrefixHandler(fastCacheProperties.getKeyPrefix());
            if (Objects.nonNull(redisProperties.getCluster())) {
                config.useSingleServer().setNameMapper(keyPrefixHandler);
            } else if (Objects.nonNull(redisProperties.getSentinel())) {
                config.useSentinelServers().setNameMapper(keyPrefixHandler);
            } else {
                config.useSingleServer().setNameMapper(keyPrefixHandler);
            }
        };
    }
}
