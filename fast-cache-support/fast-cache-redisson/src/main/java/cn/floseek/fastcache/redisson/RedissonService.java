package cn.floseek.fastcache.redisson;

import cn.floseek.fastcache.redis.SortedEntry;
import cn.floseek.fastcache.redis.RedisService;
import org.apache.commons.collections4.CollectionUtils;
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
 * Redisson 服务
 *
 * @author ChenHongwei472
 */
public class RedissonService implements RedisService {

    private final RedissonClient redissonClient;

    public RedissonService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> void setObject(final String key, final T value) {
        setObject(key, value, false);
    }

    @Override
    public <T> void setObject(final String key, final T value, final boolean saveTtl) {
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
    public <T> void setObject(final String key, final T value, final Duration duration) {
        RBatch batch = redissonClient.createBatch();
        RBucketAsync<T> bucket = batch.getBucket(key);
        bucket.setAsync(value);
        bucket.expireAsync(duration);
        batch.execute();
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
            bucket.setAsync(value, duration);
        });
        batch.execute();

    }

    @Override
    public <T> boolean setObjectIfAbsent(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent(value, duration);
    }

    @Override
    public <T> boolean setObjectIfExists(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.setIfExists(value, duration);
    }

    @Override
    public boolean expire(final String key, final long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    @Override
    public <T> boolean expire(final String key, final Duration duration) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.expire(duration);
    }

    @Override
    public <T> T getObject(final String key) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.get();
    }

    @Override
    public <T> List<T> getObjects(final Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).getAsync());
        BatchResult<?> batchResult = batch.execute();
        return batchResult.getResponses().stream()
                .map(response -> (T) response)
                .collect(Collectors.toList());
    }

    @Override
    public <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    @Override
    public boolean deleteObject(final String key) {
        return redissonClient.getBucket(key).delete();
    }

    @Override
    public <T> void deleteObjects(final Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).deleteAsync());
        batch.execute();
    }

    @Override
    public boolean isExistsObject(final String key) {
        return redissonClient.getBucket(key).isExists();
    }

    @Override
    public <T> boolean setList(final String key, final List<T> dataList) {
        RList<T> rList = redissonClient.getList(key);
        return rList.addAll(dataList);
    }

    @Override
    public <T> boolean addList(final String key, final T data) {
        RList<T> rList = redissonClient.getList(key);
        return rList.add(data);
    }

    @Override
    public <T> List<T> getList(final String key) {
        RList<T> rList = redissonClient.getList(key);
        return rList.readAll();
    }

    @Override
    public <T> List<T> getListRange(final String key, int form, int to) {
        RList<T> rList = redissonClient.getList(key);
        return rList.range(form, to);
    }

    @Override
    public <T> boolean setSet(final String key, final Set<T> dataSet) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.addAll(dataSet);
    }

    @Override
    public <T> boolean addSet(final String key, final T data) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.add(data);
    }

    @Override
    public <T> Set<T> getSet(final String key) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.readAll();
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
    public <T> boolean addSortedSet(String key, double score, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.add(score, object);
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRange(startIndex, endIndex);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRange(startScore, startScoreInclusive, endScore, endScoreInclusive);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore) {
        return this.getSortedSetValueRange(key, startScore, true, endScore, true);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRangeReversed(startIndex, endIndex);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<T> collection = scoredSortedSet.valueRangeReversed(startScore, startScoreInclusive, endScore, endScoreInclusive);
        return new ArrayList<>(collection);
    }

    @Override
    public <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore) {
        return this.getSortedSetValueRangeReversed(key, startScore, true, endScore, true);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRange(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRange(startScore, startScoreInclusive, endScore, endScoreInclusive);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore) {
        return this.getSortedSetEntryRange(key, startScore, true, endScore, true);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startIndex, endIndex);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<T>> scoredEntryCollection = scoredSortedSet.entryRangeReversed(startScore, startScoreInclusive, endScore, endScoreInclusive);
        return this.convertToSortedEntryList(scoredEntryCollection);
    }

    @Override
    public <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore) {
        return this.getSortedSetEntryRange(key, startScore, true, endScore, true);
    }

    @Override
    public <K, V> void setMap(final String key, final Map<K, V> dataMap) {
        if (dataMap != null) {
            RMap<K, V> rMap = redissonClient.getMap(key);
            rMap.putAll(dataMap);
        }
    }

    @Override
    public <T> Map<String, T> getMap(final String key) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    @Override
    public <T> Set<String> getMapKeySet(final String key) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.keySet();
    }

    @Override
    public <T> void setMapValue(final String key, final String hKey, final T value) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        rMap.put(hKey, value);
    }

    @Override
    public <T> T getMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.get(hKey);
    }

    @Override
    public <T> T deleteMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.remove(hKey);
    }

    @Override
    public <T> void deleteMapValue(final String key, final Set<String> hKeys) {
        RBatch batch = redissonClient.createBatch();
        RMapAsync<String, T> rMap = batch.getMap(key);
        for (String hKey : hKeys) {
            rMap.removeAsync(hKey);
        }
        batch.execute();
    }

    @Override
    public <K, V> Map<K, V> getMapValue(final String key, final Set<K> hKeys) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        return rMap.getAll(hKeys);
    }

    @Override
    public void setAtomicValue(String key, long value) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        atomic.set(value);
    }

    @Override
    public long getAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.get();
    }

    @Override
    public long incrementAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.incrementAndGet();
    }

    @Override
    public long decrementAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.decrementAndGet();
    }

    @Override
    public Collection<String> getKeys(final String pattern) {
        KeysScanOptions keysScanOptions = KeysScanOptions.defaults().pattern(pattern);
        Iterable<String> keys = redissonClient.getKeys().getKeys(keysScanOptions);
        return StreamSupport.stream(keys.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void deleteKeys(final String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    @Override
    public Boolean hasKey(String key) {
        RKeys rKeys = redissonClient.getKeys();
        return rKeys.countExists(key) > 0;
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
