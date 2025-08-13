package cn.floseek.fastcache.config;

import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.handler.RedisKeyPrefixHandler;
import cn.floseek.fastcache.manager.BloomFilterManager;
import cn.floseek.fastcache.manager.CacheManager;
import cn.floseek.fastcache.manager.DefaultCacheManager;
import cn.floseek.fastcache.manager.broadcast.BroadcastManager;
import cn.floseek.fastcache.manager.broadcast.impl.RedissonBroadcastManager;
import cn.floseek.fastcache.service.RedisService;
import cn.floseek.fastcache.support.redisson.RedissonService;
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

/**
 * FastCache 配置类
 *
 * @author ChenHongwei472
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FastCacheProperties.class)
public class FastCacheConfiguration {

    @Bean
    public RedisService redisService(RedissonClient redissonClient) {
        return new RedissonService(redissonClient);
    }

    @Bean
    public BroadcastManager broadcastManager(FastCacheProperties fastCacheProperties, RedissonClient redissonClient) {
        return new RedissonBroadcastManager(fastCacheProperties, redissonClient);
    }

    @Bean
    public BloomFilterManager bloomFilterManager(RedissonClient redissonClient) {
        return new BloomFilterManager(redissonClient);
    }

    @Bean(destroyMethod = "close")
    public CacheManager cacheManager(RedisService redisService, BroadcastManager broadcastManager) {
        return new DefaultCacheManager(redisService, broadcastManager);
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

            RedisKeyPrefixHandler keyPrefixHandler = new RedisKeyPrefixHandler(fastCacheProperties.getKeyPrefix());
            if (redisProperties.getCluster() != null) {
                config.useSingleServer().setNameMapper(keyPrefixHandler);
            } else if (redisProperties.getSentinel() != null) {
                config.useSentinelServers().setNameMapper(keyPrefixHandler);
            } else {
                config.useSingleServer().setNameMapper(keyPrefixHandler);
            }
        };
    }
}
