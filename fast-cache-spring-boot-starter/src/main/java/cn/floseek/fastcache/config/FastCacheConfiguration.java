package cn.floseek.fastcache.config;

import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.constant.CacheConstant;
import cn.floseek.fastcache.handler.RedisKeyPrefixHandler;
import cn.floseek.fastcache.listener.CacheMessageListener;
import cn.floseek.fastcache.manager.CacheManager;
import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import cn.floseek.fastcache.service.broadcast.RedisBroadcastService;
import cn.floseek.fastcache.service.redis.RedisService;
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
        return new RedisService(redissonClient);
    }

    @Bean
    public LocalCacheManager localCacheManager() {
        return new LocalCacheManager();
    }

    @Bean
    public BroadcastService broadcastService(RedisService redisService) {
        return new RedisBroadcastService(redisService, CacheConstant.CACHE_MESSAGE_TOPIC);
    }

    @Bean
    public CacheMessageListener cacheMessageListener(BroadcastService broadcastService, LocalCacheManager localCacheManager) {
        return new CacheMessageListener(broadcastService, localCacheManager);
    }

    @Bean
    public CacheManager cacheManager(RedisService redisService, LocalCacheManager localCacheManager, BroadcastService broadcastService) {
        return new CacheManager(redisService, localCacheManager, broadcastService);
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
