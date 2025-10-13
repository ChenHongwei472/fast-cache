package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.redis.RedisService;
import cn.floseek.fastcache.redis.SortedEntry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.redisson.api.BatchResult;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.client.protocol.ScoredEntry;

import java.time.Duration;
import java.util.ArrayList;
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
 * @author ChenHongwei472
 */
public class RedissonServiceImpl implements RedisService {

    private final RedissonClient redissonClient;

    public RedissonServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> void setObject(String key, T value) {
        this.setObject(key, value, false);
    }

    @Override
    public <T> void setObject(String key, T value, boolean saveTtl) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        if (saveTtl) {
            try {
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                long timeToLive = bucket.remainTimeToLive();
                setObject(key, value, Duration.ofMillis(timeToLive));
            }
        } else {
            bucket.set(value);
        }
    }

    @Override
    public <T> void setObject(String key, T value, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        if (DurationUtils.isPositive(duration)) {
            bucket.set(value, duration);
        } else {
            bucket.set(value);
        }
    }

    @Override
    public <T> void setObjects(Map<String, T> objects) {
        RBatch batch = redissonClient.createBatch();
        objects.forEach((key, value) -> {
            RBucketAsync<T> bucket = batch.getBucket(key);
            bucket.setAsync(value);
        });
        batch.execute();
    }

    @Override
    public <T> void setObjects(Map<String, T> objects, Duration duration) {
        RBatch batch = redissonClient.createBatch();
        objects.forEach((key, value) -> {
            RBucketAsync<T> bucket = batch.getBucket(key);
            if (DurationUtils.isPositive(duration)) {
                bucket.setAsync(value, duration);
            } else {
                bucket.setAsync(value);
            }
        });
        batch.execute();

    }

    @Override
    public <T> boolean setObjectIfAbsent(String key, T value, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        if (DurationUtils.isPositive(duration)) {
            return bucket.setIfAbsent(value, duration);
        } else {
            return bucket.setIfAbsent(value);
        }
    }

    @Override
    public <T> boolean setObjectIfExists(String key, T value, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        if (DurationUtils.isPositive(duration)) {
            return bucket.setIfExists(value, duration);
        } else {
            return bucket.setIfExists(value);
        }
    }

    @Override
    public <T> boolean expire(String key, Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.expire(duration);
    }

    @Override
    public <T> T getObject(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getObjects(Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).getAsync());
        BatchResult<?> batchResult = batch.execute();
        return batchResult.getResponses().stream()
                .map(response -> (T) response)
                .collect(Collectors.toList());
    }

    @Override
    public <T> long getTimeToLive(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.remainTimeToLive();
    }

    @Override
    public boolean deleteObject(String key) {
        return redissonClient.getBucket(key).delete();
    }

    @Override
    public <T> void deleteObjects(Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).deleteAsync());
        batch.execute();
    }

    @Override
    public boolean existsObject(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    @Override
    public <T> boolean setList(String key, List<T> dataList) {
        RList<T> list = redissonClient.getList(key);
        return list.addAll(dataList);
    }

    @Override
    public <T> boolean addList(String key, T data) {
        RList<T> list = redissonClient.getList(key);
        return list.add(data);
    }

    @Override
    public <T> List<T> getList(String key) {
        RList<T> list = redissonClient.getList(key);
        return list.readAll();
    }

    @Override
    public <T> List<T> getListRange(String key, int form, int to) {
        RList<T> list = redissonClient.getList(key);
        return list.range(form, to);
    }

    @Override
    public <T> boolean setSet(String key, Set<T> dataSet) {
        RSet<T> set = redissonClient.getSet(key);
        return set.addAll(dataSet);
    }

    @Override
    public <T> boolean addSet(String key, T data) {
        RSet<T> set = redissonClient.getSet(key);
        return set.add(data);
    }

    @Override
    public <T> Set<T> getSet(String key) {
        RSet<T> set = redissonClient.getSet(key);
        return set.readAll();
    }

    @Override
    public <T> Integer getSortedSetRank(String key, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.rank(object);
    }

    @Override
    public <T> Integer getSortedSetReRank(String key, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.revRank(object);
    }

    @Override
    public <T> Double getSortedSetScore(String key, T element) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.getScore(element);
    }

    @Override
    public <T> List<Double> getSortedSetScore(String key, List<T> elements) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.getScore(elements);
    }

    @Override
    public <T> boolean removeSortedSet(String key, T element) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.remove(element);
    }

    @Override
    public <T> int addSortedSet(String key, Map<T, Double> objects) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.addAll(objects);
    }

    @Override
    public <T> int addSortedSet(String key, Map<T, Double> objects, Duration duration) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        int result = scoredSortedSet.addAll(objects);
        if (DurationUtils.isPositive(duration)) {
            scoredSortedSet.expire(duration);
        }
        return result;
    }

    @Override
    public <T> boolean addSortedSet(String key, double score, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.add(score, object);
    }

    @Override
    public <T> boolean addSortedSet(String key, double score, T object, Duration duration) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        boolean result = scoredSortedSet.add(score, object);
        if (DurationUtils.isPositive(duration)) {
            scoredSortedSet.expire(duration);
        }
        return result;
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRange(startIndex, endIndex);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRange(startScore, true, endScore, true);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRange(startScore, true, endScore, true, offset, count);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRangeReversed(startIndex, endIndex);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRangeReversed(startScore, true, endScore, true, offset, count);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRange(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRange(startScore, true, endScore, true);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRange(startScore, true, endScore, true, offset, count);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startScore, true, endScore, true);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore, int offset, int count) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startScore, true, endScore, true, offset, count);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <K, V> void setMap(String key, Map<K, V> dataMap) {
        if (MapUtils.isNotEmpty(dataMap)) {
            RMap<K, V> map = redissonClient.getMap(key);
            map.putAll(dataMap);
        }
    }

    @Override
    public <T> Map<String, T> getMap(String key) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.getAll(map.keySet());
    }

    @Override
    public <T> Set<String> getMapKeySet(String key) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.keySet();
    }

    @Override
    public <T> void setMapValue(String key, String hKey, T value) {
        RMap<String, T> map = redissonClient.getMap(key);
        map.put(hKey, value);
    }

    @Override
    public <T> T getMapValue(String key, String hKey) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.get(hKey);
    }

    @Override
    public <T> T deleteMapValue(String key, String hKey) {
        RMap<String, T> map = redissonClient.getMap(key);
        return map.remove(hKey);
    }

    @Override
    public <T> void deleteMapValue(String key, Set<String> hKeys) {
        RBatch batch = redissonClient.createBatch();
        RMapAsync<String, T> map = batch.getMap(key);
        for (String hKey : hKeys) {
            map.removeAsync(hKey);
        }
        batch.execute();
    }

    @Override
    public <K, V> Map<K, V> getMapValue(String key, Set<K> hKeys) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.getAll(hKeys);
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
     * 转换为 {@link SortedEntry} 列表
     *
     * @param scoredEntryCollection {@link ScoredEntry} 集合
     * @param <T>                   值类型
     * @return {@link SortedEntry} 列表
     */
    private <T> List<SortedEntry<T>> convertToSortedEntryList(Collection<ScoredEntry<T>> scoredEntryCollection) {
        if (CollectionUtils.isEmpty(scoredEntryCollection)) {
            return Collections.emptyList();
        }

        return scoredEntryCollection.stream()
                .map(scoredEntry -> new SortedEntry<>(scoredEntry.getScore(), scoredEntry.getValue()))
                .collect(Collectors.toList());
    }

}
