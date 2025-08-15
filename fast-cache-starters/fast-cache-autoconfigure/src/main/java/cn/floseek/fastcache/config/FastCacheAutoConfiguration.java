package cn.floseek.fastcache.config;

import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.manager.CacheManager;
import cn.floseek.fastcache.manager.DefaultCacheManager;
import cn.floseek.fastcache.manager.broadcast.BroadcastManager;
import cn.floseek.fastcache.service.RedisService;
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
