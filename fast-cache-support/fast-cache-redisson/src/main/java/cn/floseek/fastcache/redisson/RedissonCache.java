package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.cache.AbstractRemoteCache;
import cn.floseek.fastcache.config.CacheConfig;
import cn.floseek.fastcache.serializer.ValueSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;

import java.nio.charset.Charset;
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
public class RedissonCache<K, V> extends AbstractRemoteCache<K, V> {

    protected final RedissonClient redissonClient;

    private final Duration expireTime;

    private final ValueSerializer valueSerializer;

    public RedissonCache(CacheConfig<K, V> config, RedissonClient redissonClient) {
        super(config);
        this.redissonClient = redissonClient;
        this.expireTime = config.getExpireTime();
        this.valueSerializer = config.getValueSerializer();
    }

    @Override
    public V get(K key) {
        RBucket<byte[]> bucket = redissonClient.getBucket(this.getCacheKey(key), this.getCodec());
        byte[] bytes = bucket.get();
        return valueSerializer.deserialize(bytes);
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        List<K> keyList = new ArrayList<>(keys);

        RBatch batch = redissonClient.createBatch();
        keyList.forEach(key -> batch.getBucket(this.getCacheKey(key), this.getCodec()).getAsync());
        BatchResult<?> batchResult = batch.execute();

        List<byte[]> objectList = batchResult.getResponses().stream()
                .map(response -> (byte[]) response)
                .toList();

        Map<K, V> valueMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            byte[] value = objectList.get(i);
            if (Objects.nonNull(value)) {
                valueMap.put(key, valueSerializer.deserialize(value));
            }
        }
        return valueMap;
    }

    @Override
    public void put(K key, V value) {
        RBucket<byte[]> bucket = redissonClient.getBucket(this.getCacheKey(key), this.getCodec());
        if (Objects.nonNull(expireTime) && DurationUtils.isPositive(expireTime)) {
            bucket.set(valueSerializer.serialize(value), expireTime);
        } else {
            bucket.set(valueSerializer.serialize(value));
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        RBatch batch = redissonClient.createBatch();
        map.forEach((key, value) -> {
            RBucketAsync<byte[]> bucket = batch.getBucket(this.getCacheKey(key), this.getCodec());
            if (Objects.nonNull(expireTime) && DurationUtils.isPositive(expireTime)) {
                bucket.setAsync(valueSerializer.serialize(value), expireTime);
            } else {
                bucket.setAsync(valueSerializer.serialize(value));
            }
        });
        batch.execute();
    }

    @Override
    public void remove(K key) {
        redissonClient.getBucket(this.getCacheKey(key), this.getCodec()).delete();
    }

    @Override
    public void removeAll(Collection<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> redissonClient.getBucket(this.getCacheKey(key), this.getCodec()).deleteAsync());
        batch.execute();
    }

    /**
     * 获取编解码器
     *
     * @return {@link Codec}
     */
    private Codec getCodec() {
        return ByteArrayCodec.INSTANCE;
    }

    /**
     * 获取缓存键
     *
     * @param key 缓存键
     * @return 缓存键
     */
    private String getCacheKey(K key) {
        byte[] keyBytes = super.buildCacheKey(key);
        return new String(keyBytes, Charset.defaultCharset());
    }

}
