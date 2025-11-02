package cn.floseek.fastcache.config;

import cn.floseek.fastcache.cache.CacheManager;
import cn.floseek.fastcache.cache.DefaultCacheManager;
import cn.floseek.fastcache.cache.builder.CacheBuilderManager;
import cn.floseek.fastcache.cache.builder.LocalCacheBuilder;
import cn.floseek.fastcache.cache.builder.RemoteCacheBuilder;
import cn.floseek.fastcache.cache.local.CaffeineCacheBuilder;
import cn.floseek.fastcache.cache.local.GuavaCacheBuilder;
import cn.floseek.fastcache.config.properties.FastCacheProperties;
import cn.floseek.fastcache.lock.LockTemplate;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * FastCache 自动配置类
 *
 * @author ChenHongwei472
 */
@Configuration
@EnableConfigurationProperties(FastCacheProperties.class)
public class FastCacheAutoConfiguration {

    @Resource
    private LockTemplate lockTemplate;

    @Bean(destroyMethod = "close")
    public <K, V> CacheManager cacheManager(FastCacheProperties fastCacheProperties,
                                            CacheBuilderManager<K, V> cacheBuilderManager) {
        return new DefaultCacheManager(fastCacheProperties, cacheBuilderManager, lockTemplate);
    }

    @Bean
    public <K, V> CacheBuilderManager<K, V> cacheBuilderManager(List<LocalCacheBuilder<K, V>> localCacheBuilders,
                                                                List<RemoteCacheBuilder<K, V>> remoteCacheBuilders) {
        CacheBuilderManager<K, V> cacheBuilderManager = new CacheBuilderManager<>();

        // 注册本地缓存构建器
        for (LocalCacheBuilder<K, V> localCacheBuilder : localCacheBuilders) {
            cacheBuilderManager.registerLocalCacheBuilder(localCacheBuilder.getProvider(), localCacheBuilder);
        }

        // 注册分布式缓存构建器
        for (RemoteCacheBuilder<K, V> remoteCacheBuilder : remoteCacheBuilders) {
            cacheBuilderManager.registerRemoteCacheBuilder(remoteCacheBuilder.getProvider(), remoteCacheBuilder);
        }

        return cacheBuilderManager;
    }

    @Bean
    public <K, V> LocalCacheBuilder<K, V> caffeineCacheBuilder() {
        return new CaffeineCacheBuilder<>();
    }

    @Bean
    public <K, V> LocalCacheBuilder<K, V> guavaCacheBuilder() {
        return new GuavaCacheBuilder<>();
    }

}
