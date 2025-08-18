package cn.floseek.fastcache.config;

import cn.floseek.fastcache.bloomfilter.BloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.BloomFilterManager;
import cn.floseek.fastcache.bloomfilter.impl.GuavaBloomFilterFactory;
import cn.floseek.fastcache.bloomfilter.template.BloomFilterTemplate;
import cn.floseek.fastcache.config.properties.BloomFilterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
            BloomFilterTemplate bloomFilterTemplate) {
        BloomFilterFactory bloomFilterFactory = bloomFilterTemplate.get(bloomFilterProperties.getType());
        if (bloomFilterFactory == null) {
            String message = String.format("Unsupported bloom filter type: %s, please check the dependency", bloomFilterProperties.getType());
            throw new IllegalArgumentException(message);
        }

        return new BloomFilterManager(bloomFilterFactory,
                bloomFilterProperties.getExpectedInsertions(),
                bloomFilterProperties.getFalsePositiveProbability());
    }

    @Bean
    public BloomFilterTemplate bloomFilterTemplate(List<BloomFilterFactory> bloomFilterFactoryList) {
        BloomFilterTemplate bloomFilterTemplate = new BloomFilterTemplate();
        for (BloomFilterFactory bloomFilterFactory : bloomFilterFactoryList) {
            bloomFilterTemplate.register(bloomFilterFactory.getType(), bloomFilterFactory);
        }
        return bloomFilterTemplate;
    }

    @Bean
    public BloomFilterFactory guavaBloomFilterFactory() {
        return new GuavaBloomFilterFactory();
    }
}
