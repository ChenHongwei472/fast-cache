package cn.floseek.fastcache.config;

import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.lock.LockTemplate;
import cn.floseek.fastcache.redis.RedisService;
import cn.floseek.fastcache.redisson.RedissonBloomFilterFactory;
import cn.floseek.fastcache.redisson.RedissonCacheBuilder;
import cn.floseek.fastcache.redisson.RedissonLockTemplate;
import cn.floseek.fastcache.redisson.RedissonServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public BloomFilterFactory redissonBloomFilterFactory(RedissonClient redissonClient) {
        return new RedissonBloomFilterFactory(redissonClient);
    }

    @Bean
    public <K, V> RemoteCacheBuilder<K, V> remoteCacheBuilder(RedissonClient redissonClient) {
        return new RedissonCacheBuilder<>(redissonClient);
    }

    @Bean
    public LockTemplate lockTemplate(RedissonClient redissonClient) {
        return new RedissonLockTemplate(redissonClient);
    }

    @Bean
    public RedisService redissonService(RedissonClient redissonClient, FastCacheProperties fastCacheProperties) {
        return new RedissonServiceImpl(
                redissonClient,
                fastCacheProperties.getRemote().getKeyConverter().getKeyConverter(),
                fastCacheProperties.getRemote().getSerializer().getSerializer()
        );
    }

}
