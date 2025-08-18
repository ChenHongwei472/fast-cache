package cn.floseek.fastcache.config;

import cn.floseek.fastcache.config.properties.BloomFilterProperties;
import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.BloomFilterType;
import cn.floseek.fastcache.bloomfilter.BloomFilterManager;
import cn.floseek.fastcache.bloomfilter.impl.GuavaBloomFilterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 布隆过滤器自动配置类
 *
 * @author ChenHongwei472
 */
@Configuration
@ConditionalOnClass(BloomFilterManager.class)
@EnableConfigurationProperties(BloomFilterProperties.class)
public class BloomFilterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BloomFilterManager bloomFilterManager(
            BloomFilterProperties bloomFilterProperties,
            List<BloomFilterFactory> bloomFilterFactoryList) {
        Map<BloomFilterType, BloomFilterFactory> bloomFilterFactoryMap = bloomFilterFactoryList.stream()
                .collect(Collectors.toMap(BloomFilterFactory::getType, Function.identity()));

        BloomFilterFactory bloomFilterFactory = bloomFilterFactoryMap.get(bloomFilterProperties.getType());
        if (bloomFilterFactory == null) {
            throw new IllegalArgumentException(
                    String.format("不支持的布隆过滤器类型: %s，请检查是否引入相关依赖", bloomFilterProperties.getType())
            );
        }

        return new BloomFilterManager(bloomFilterFactory,
                bloomFilterProperties.getExpectedInsertions(),
                bloomFilterProperties.getFalsePositiveProbability());
    }

    @Bean
    public GuavaBloomFilterFactory guavaBloomFilterFactory() {
        return new GuavaBloomFilterFactory();
    }
}
