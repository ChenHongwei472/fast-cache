package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.cache.AbstractRemoteCache;
import cn.floseek.fastcache.cache.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Redisson 缓存实现
 *
 * @author ChenHongwei472
 */
@Slf4j
public class RedissonCache<K, V> extends AbstractRemoteCache<K, V> {

    protected final RedissonClient redissonClient;

    private final Duration expireTime;

    public RedissonCache(CacheConfig<K, V> config, RedissonClient redissonClient) {
        super(config);
        this.redissonClient = redissonClient;
        this.expireTime = config.getExpireTime();
    }

    @Override
    public V get(K key) {
        RBucket<V> bucket = redissonClient.getBucket(this.generateCacheKey(key));
        return bucket.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> getAll(Collection<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        List<K> keyList = new ArrayList<>(keys);

        RBatch batch = redissonClient.createBatch();
        keyList.forEach(k -> batch.getBucket(this.generateCacheKey(k)).getAsync());
        BatchResult<?> batchResult = batch.execute();

        List<V> objectList = batchResult.getResponses().stream()
                .map(response -> (V) response)
                .toList();

        Map<K, V> valueMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            V value = objectList.get(i);
            if (Objects.nonNull(value)) {
                valueMap.put(key, value);
            }
        }
        return valueMap;
    }

    @Override
    public void put(K key, V value) {
        RBucket<V> bucket = redissonClient.getBucket(this.generateCacheKey(key));
        if (Objects.nonNull(expireTime) && DurationUtils.isPositive(expireTime)) {
            bucket.set(value, expireTime);
        } else {
            bucket.set(value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        RBatch batch = redissonClient.createBatch();
        map.forEach((key, value) -> {
            RBucketAsync<V> bucket = batch.getBucket(this.generateCacheKey(key));
            if (Objects.nonNull(expireTime) && DurationUtils.isPositive(expireTime)) {
                bucket.setAsync(value, expireTime);
            } else {
                bucket.setAsync(value);
            }
        });
        batch.execute();
    }

    @Override
    public void remove(K key) {
        redissonClient.getBucket(this.generateCacheKey(key)).delete();
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> redissonClient.getBucket(this.generateCacheKey(key)).deleteAsync());
        batch.execute();
    }
}
