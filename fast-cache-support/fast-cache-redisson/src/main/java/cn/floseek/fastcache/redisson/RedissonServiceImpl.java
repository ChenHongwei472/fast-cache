package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.cache.converter.KeyConverter;
import cn.floseek.fastcache.cache.serializer.Serializer;
import cn.floseek.fastcache.common.CacheConstant;
import cn.floseek.fastcache.redis.RedisService;
import cn.floseek.fastcache.redis.SortedEntry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.redisson.api.BatchResult;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.protocol.ScoredEntry;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Redisson 服务实现
 *
 * @param redissonClient Redisson 客户端
 * @param keyConverter   键名转换器
 * @param serializer     序列化器
 * @author ChenHongwei472
 */
public record RedissonServiceImpl(
        RedissonClient redissonClient,
        KeyConverter keyConverter,
        Serializer serializer
) implements RedisService {

    @Override
    public <T> void setObject(String key, T value) {
        this.setObject(key, value, CacheConstant.NEVER_EXPIRE);
    }

    @Override
    public <T> void setObject(String key, T value, Duration duration) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        byte[] serializedValue = serializer.serialize(value);
        if (DurationUtils.isPositive(duration)) {
            bucket.set(serializedValue, duration);
        } else {
            bucket.set(serializedValue);
        }
    }

    @Override
    public <T> void setObjects(Map<String, T> objects) {
        this.setObjects(objects, CacheConstant.NEVER_EXPIRE);
    }

    @Override
    public <T> void setObjects(Map<String, T> objects, Duration duration) {
        RBatch batch = redissonClient.createBatch();
        objects.forEach((key, value) -> {
            RBucketAsync<byte[]> bucket = this.getByteArrayBucket(batch, key, keyConverter);
            byte[] serializedValue = serializer.serialize(value);
            if (DurationUtils.isPositive(duration)) {
                bucket.setAsync(serializedValue, duration);
            } else {
                bucket.setAsync(serializedValue);
            }
        });
        batch.execute();
    }

    @Override
    public <T> boolean setObjectIfAbsent(String key, T value, Duration duration) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        byte[] serializedValue = serializer.serialize(value);
        if (DurationUtils.isPositive(duration)) {
            return bucket.setIfAbsent(serializedValue, duration);
        } else {
            return bucket.setIfAbsent(serializedValue);
        }
    }

    @Override
    public <T> boolean setObjectIfExists(String key, T value, Duration duration) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        byte[] serializedValue = serializer.serialize(value);
        if (DurationUtils.isPositive(duration)) {
            return bucket.setIfExists(serializedValue, duration);
        } else {
            return bucket.setIfExists(serializedValue);
        }
    }

    @Override
    public boolean expire(String key, Duration duration) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        return bucket.expire(duration);
    }

    @Override
    public <T> T getObject(String key) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        return serializer.deserialize(bucket.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getObjects(Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> {
            RBucketAsync<byte[]> bucket = this.getByteArrayBucket(batch, key, keyConverter);
            bucket.getAsync();
        });
        BatchResult<?> batchResult = batch.execute();
        return batchResult.getResponses().stream()
                .map(response -> (T) serializer.deserialize((byte[]) response))
                .collect(Collectors.toList());
    }

    @Override
    public long getTimeToLive(String key) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        return bucket.remainTimeToLive();
    }

    @Override
    public boolean deleteObject(String key) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        return bucket.delete();
    }

    @Override
    public void deleteObjects(Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> {
            RBucketAsync<byte[]> bucket = this.getByteArrayBucket(batch, key, keyConverter);
            bucket.deleteAsync();
        });
        batch.execute();
    }

    @Override
    public boolean existsObject(String key) {
        RBucket<byte[]> bucket = this.getByteArrayBucket(redissonClient, key, keyConverter);
        return bucket.isExists();
    }

    @Override
    public <T> boolean setList(String key, List<T> dataList) {
        RList<byte[]> list = this.getByteArrayList(redissonClient, key, keyConverter);
        List<byte[]> valueBytes = dataList.stream().map(serializer::serialize).toList();
        return list.addAll(valueBytes);
    }

    @Override
    public <T> boolean addList(String key, T data) {
        RList<byte[]> list = this.getByteArrayList(redissonClient, key, keyConverter);
        return list.add(serializer.serialize(data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        RList<byte[]> list = this.getByteArrayList(redissonClient, key, keyConverter);
        List<byte[]> valueBytes = list.readAll();
        return valueBytes.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getListRange(String key, int form, int to) {
        RList<byte[]> list = this.getByteArrayList(redissonClient, key, keyConverter);
        List<byte[]> valueBytes = list.range(form, to);
        return valueBytes.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .toList();
    }

    @Override
    public <T> boolean setSet(String key, Set<T> dataSet) {
        RSet<byte[]> set = this.getByteArraySet(redissonClient, key, keyConverter);
        Set<byte[]> valueBytes = dataSet.stream().map(serializer::serialize).collect(Collectors.toSet());
        return set.addAll(valueBytes);
    }

    @Override
    public <T> boolean addSet(String key, T data) {
        RSet<byte[]> set = this.getByteArraySet(redissonClient, key, keyConverter);
        return set.add(serializer.serialize(data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> getSet(String key) {
        RSet<byte[]> set = this.getByteArraySet(redissonClient, key, keyConverter);
        Set<byte[]> valueBytes = set.readAll();
        return valueBytes.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toSet());
    }

    @Override
    public <T> Integer getSortedSetRank(String key, T object) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        return scoredSortedSet.rank(serializer.serialize(object));
    }

    @Override
    public <T> Integer getSortedSetReRank(String key, T object) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        return scoredSortedSet.revRank(serializer.serialize(object));
    }

    @Override
    public <T> Double getSortedSetScore(String key, T element) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        return scoredSortedSet.getScore(serializer.serialize(element));
    }

    @Override
    public <T> List<Double> getSortedSetScore(String key, List<T> elements) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        List<byte[]> valueBytes = elements.stream().map(serializer::serialize).toList();
        return scoredSortedSet.getScore(valueBytes);
    }

    @Override
    public <T> boolean removeSortedSet(String key, T element) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        return scoredSortedSet.remove(serializer.serialize(element));
    }

    @Override
    public <T> int addSortedSet(String key, Map<T, Double> objects) {
        return this.addSortedSet(key, objects, CacheConstant.NEVER_EXPIRE);
    }

    @Override
    public <T> int addSortedSet(String key, Map<T, Double> objects, Duration duration) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Map<byte[], Double> bytesMap = objects.entrySet().stream()
                .collect(Collectors.toMap(entry -> serializer.serialize(entry.getKey()), Map.Entry::getValue));
        int result = scoredSortedSet.addAll(bytesMap);
        if (DurationUtils.isPositive(duration)) {
            scoredSortedSet.expire(duration);
        }
        return result;
    }

    @Override
    public <T> boolean addSortedSet(String key, double score, T object) {
        return this.addSortedSet(key, score, object, CacheConstant.NEVER_EXPIRE);
    }

    @Override
    public <T> boolean addSortedSet(String key, double score, T object, Duration duration) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        boolean result = scoredSortedSet.add(score, serializer.serialize(object));
        if (DurationUtils.isPositive(duration)) {
            scoredSortedSet.expire(duration);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRange(startIndex, endIndex);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRange(startScore, true, endScore, true);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRange(startScore, true, endScore, true, offset, count);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRangeReversed(startIndex, endIndex);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<byte[]> collection = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true, offset, count);
        return collection.stream()
                .map(bytes -> (T) serializer.deserialize(bytes))
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRange(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRange(startScore, true, endScore, true);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRange(startScore, true, endScore, true, offset, count);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startScore, true, endScore, true);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<byte[]> scoredSortedSet = this.getByteArrayScoredSortedSet(redissonClient, key, keyConverter);
        Collection<ScoredEntry<byte[]>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startScore, true, endScore, true, offset, count);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public void setAtomicValue(String key, long value) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        atomicLong.set(value);
    }

    @Override
    public long getAtomicValue(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.get();
    }

    @Override
    public long incrementAtomicValue(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    @Override
    public long decrementAtomicValue(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    @Override
    public Collection<String> getKeys(String pattern) {
        KeysScanOptions keysScanOptions = KeysScanOptions.defaults().pattern(pattern);
        Iterable<String> keys = redissonClient.getKeys().getKeys(keysScanOptions);
        return StreamSupport.stream(keys.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void deleteKeys(String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    @Override
    public Boolean hasKey(String key) {
        RKeys keys = redissonClient.getKeys();
        return keys.countExists(key) > 0;
    }

    /**
     * 获取字节数组缓存桶
     *
     * @param redissonClient Redisson 客户端
     * @param key            缓存键
     * @param keyConverter   键名转换器
     * @return 字节数组缓存桶
     */
    private RBucket<byte[]> getByteArrayBucket(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getBucket(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    /**
     * 获取字节数组缓存桶
     *
     * @param batch        {@link RBatch}
     * @param key          缓存键
     * @param keyConverter 缓存键转换器
     * @return 字节数组缓存桶
     */
    private RBucketAsync<byte[]> getByteArrayBucket(RBatch batch, String key, KeyConverter keyConverter) {
        return batch.getBucket(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    /**
     * 获取字节数组列表缓存桶
     *
     * @param redissonClient Redisson 客户端
     * @param key            缓存键
     * @param keyConverter   缓存键转换器
     * @return 字节数组列表缓存桶
     */
    private RList<byte[]> getByteArrayList(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getList(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    /**
     * 获取字节数组集合缓存桶
     *
     * @param redissonClient Redisson 客户端
     * @param key            缓存键
     * @param keyConverter   缓存键转换器
     * @return 字节数组集合缓存桶
     */
    private RSet<byte[]> getByteArraySet(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getSet(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    /**
     * 获取字节数组排序集合缓存桶
     *
     * @param redissonClient Redisson 客户端
     * @param key            缓存键
     * @param keyConverter   缓存键转换器
     * @return 字节数组排序集合缓存桶
     */
    private RScoredSortedSet<byte[]> getByteArrayScoredSortedSet(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getScoredSortedSet(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    /**
     * 转换为 {@link SortedEntry} 列表
     *
     * @param scoredEntryCollection {@link ScoredEntry} 集合
     * @param <T>                   值类型
     * @return {@link SortedEntry} 列表
     */
    @SuppressWarnings("unchecked")
    private <T> List<SortedEntry<T>> convertToSortedEntryList(Collection<ScoredEntry<byte[]>> scoredEntryCollection) {
        if (CollectionUtils.isEmpty(scoredEntryCollection)) {
            return Collections.emptyList();
        }

        return scoredEntryCollection.stream()
                .map(scoredEntry -> new SortedEntry<>(scoredEntry.getScore(), (T) serializer.deserialize(scoredEntry.getValue())))
                .collect(Collectors.toList());
    }

}
