package cn.floseek.fastcache.config;

import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.DefaultCacheManager;
import cn.floseek.fastcache.broadcast.BroadcastManager;
import cn.floseek.fastcache.redis.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FastCache 自动配置类
 *
 * @author ChenHongwei472
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FastCacheProperties.class)
public class FastCacheAutoConfiguration {

    @Resource
    private RedisService redisService;

    @Resource
    private BroadcastManager broadcastManager;

    @Bean(destroyMethod = "close")
    public CacheManager cacheManager() {
        return new DefaultCacheManager(redisService, broadcastManager);
    }
}
