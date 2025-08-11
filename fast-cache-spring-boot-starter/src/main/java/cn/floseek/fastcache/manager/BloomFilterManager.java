package cn.floseek.fastcache.manager;

import cn.floseek.fastcache.model.BloomFilterConfig;
import cn.floseek.fastcache.service.bloomfilter.BloomFilterService;
import cn.floseek.fastcache.service.bloomfilter.impl.RedissonBloomFilterService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器管理器
 *
 * @author ChenHongwei472
 */
@RequiredArgsConstructor
public class BloomFilterManager {

    private final RedissonClient redissonClient;

    /**
     * 布隆过滤器服务映射，key：键名，value：布隆过滤器服务实例
     */
    private final Map<String, BloomFilterService> bloomFilterServiceMap = new ConcurrentHashMap<>();

    /**
     * 获取或创建布隆过滤器服务实例
     *
     * @param bloomFilterConfig 布隆过滤器配置
     * @return 布隆过滤器服务实例
     */
    public BloomFilterService getOrCreateBloomFilterService(BloomFilterConfig bloomFilterConfig) {
        String key = bloomFilterConfig.getKey();
        if (bloomFilterServiceMap.containsKey(key)) {
            return bloomFilterServiceMap.get(key);
        }

        BloomFilterService bloomFilterService = new RedissonBloomFilterService(redissonClient, bloomFilterConfig);
        bloomFilterServiceMap.put(key, bloomFilterService);
        return bloomFilterService;
    }

    /**
     * 获取布隆过滤器服务实例
     *
     * @param key 键名
     * @return 布隆过滤器服务实例
     */
    public BloomFilterService getBloomFilterService(String key) {
        return bloomFilterServiceMap.get(key);
    }
}
