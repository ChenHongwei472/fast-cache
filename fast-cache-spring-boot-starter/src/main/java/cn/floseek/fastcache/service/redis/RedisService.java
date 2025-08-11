package cn.floseek.fastcache.service.redis;

import lombok.RequiredArgsConstructor;
import org.redisson.api.BatchResult;
import org.redisson.api.ObjectListener;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RQueue;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.client.protocol.ScoredEntry;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Redis 服务
 *
 * @author ChenHongwei472
 */
@RequiredArgsConstructor
public class RedisService {

    private final RedissonClient redissonClient;

    /**
     * 使用令牌桶算法实现速率限制
     *
     * @param key          键名
     * @param mode         速率模式
     * @param rate         速率限制值
     * @param rateInterval 速率限制的时间间隔
     * @return 如果获取令牌成功，返回剩余可用令牌数；否则返回 -1
     */
    public long getRateLimiter(String key, RateType mode, long rate, Duration rateInterval) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(mode, rate, rateInterval);
        if (rateLimiter.tryAcquire()) {
            return rateLimiter.availablePermits();
        } else {
            return -1L;
        }
    }

    /**
     * 获取分布式锁
     *
     * @param key 键名
     * @return 分布式锁
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * 获取布隆过滤器
     *
     * @param key 键名
     * @param <T> 对象类型
     * @return {@link RBloomFilter}
     */
    public <T> RBloomFilter<T> getBloomFilter(String key) {
        return redissonClient.getBloomFilter(key);
    }

    /**
     * 获取布隆过滤器
     *
     * @param key                      键名
     * @param expectedInsertions       预计插入元素数量
     * @param falsePositiveProbability 期望误差率
     * @param <T>                      对象类型
     * @return {@link RBloomFilter}
     */
    public <T> RBloomFilter<T> getBloomFilter(String key, long expectedInsertions, double falsePositiveProbability) {
        RBloomFilter<T> bloomFilter = getBloomFilter(key);
        bloomFilter.tryInit(expectedInsertions, falsePositiveProbability);
        return bloomFilter;
    }

    /**
     * 获取队列
     *
     * @param key 键名
     * @param <T> 元素类型
     * @return {@link RQueue}
     */
    public <T> RQueue<T> getQueue(String key) {
        return redissonClient.getQueue(key);
    }

    /**
     * 获取阻塞队列
     *
     * @param key 键名
     * @param <T> 元素类型
     * @return {@link RBlockingQueue}
     */
    public <T> RBlockingQueue<T> getBlockingQueue(String key) {
        return redissonClient.getBlockingQueue(key);
    }

    /**
     * 获取延迟队列
     *
     * @param blockingQueue {@link RBlockingQueue}
     * @param <T>           元素类型
     * @return {@link RDelayedQueue}
     */
    public <T> RDelayedQueue<T> getDelayedQueue(RBlockingQueue<T> blockingQueue) {
        return redissonClient.getDelayedQueue(blockingQueue);
    }

    /**
     * 获取主题实例
     *
     * @param key 键名
     * @return {@link RTopic}
     */
    public RTopic getTopic(String key) {
        return redissonClient.getTopic(key);
    }

    /**
     * 获取 {@link RedissonClient} 实例
     *
     * @return {@link RedissonClient}
     */
    public RedissonClient getClient() {
        return redissonClient;
    }

    /**
     * 缓存对象
     *
     * @param key   键名
     * @param value 要缓存的对象
     */
    public <T> void setObject(final String key, final T value) {
        setObject(key, value, false);
    }

    /**
     * 缓存对象
     *
     * @param key     键名
     * @param value   要缓存的对象
     * @param saveTtl 是否保留 TTL 有效期
     */
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

    /**
     * 缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     */
    public <T> void setObject(final String key, final T value, final Duration duration) {
        RBatch batch = redissonClient.createBatch();
        RBucketAsync<T> bucket = batch.getBucket(key);
        bucket.setAsync(value);
        bucket.expireAsync(duration);
        batch.execute();
    }

    /**
     * 如果键不存在，则缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     * @return boolean
     */
    public <T> boolean setObjectIfAbsent(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent(value, duration);
    }

    /**
     * 如果键已存在，则缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     * @return boolean
     */
    public <T> boolean setObjectIfExists(final String key, final T value, final Duration duration) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.setIfExists(value, duration);
    }

    /**
     * 添加对象监听器
     *
     * @param key      键名
     * @param listener 监听器
     */
    public <T> void addObjectListener(final String key, final ObjectListener listener) {
        RBucket<T> result = redissonClient.getBucket(key);
        result.addListener(listener);
    }

    /**
     * 设置键的过期时间
     *
     * @param key     键名
     * @param timeout 过期时间，单位为秒
     * @return boolean
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置键的过期时间
     *
     * @param key      键名
     * @param duration 过期时间
     * @return boolean
     */
    public <T> boolean expire(final String key, final Duration duration) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.expire(duration);
    }

    /**
     * 获取缓存的对象
     *
     * @param key 键名
     * @return 缓存的对象
     */
    public <T> T getObject(final String key) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.get();
    }

    /**
     * 批量获取缓存的对象
     *
     * @param keys 键名的集合
     * @param <T>  对象类型
     * @return 缓存的对象集合
     */
    public <T> List<T> getObjects(final Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).getAsync());
        BatchResult<?> batchResult = batch.execute();
        return batchResult.getResponses().stream()
                .map(response -> (T) response)
                .collect(Collectors.toList());
    }

    /**
     * 获取键的剩余存活时间
     *
     * @param key 键名
     * @return 剩余存活时间
     */
    public <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = redissonClient.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    /**
     * 删除缓存的对象
     *
     * @param key 键名
     * @return boolean
     */
    public boolean deleteObject(final String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 删除缓存的对象
     *
     * @param keys 键名的集合
     */
    public <T> void deleteObjects(final Collection<String> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key).deleteAsync());
        batch.execute();
    }

    /**
     * 检查缓存的对象是否存在
     *
     * @param key 键名
     * @return boolean
     */
    public boolean isExistsObject(final String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 缓存列表
     *
     * @param key      键名
     * @param dataList 要添加到列表的数据列表
     * @return boolean
     */
    public <T> boolean setList(final String key, final List<T> dataList) {
        RList<T> rList = redissonClient.getList(key);
        return rList.addAll(dataList);
    }

    /**
     * 向列表添加一个元素
     *
     * @param key  键名
     * @param data 要添加的数据
     * @return boolean
     */
    public <T> boolean addList(final String key, final T data) {
        RList<T> rList = redissonClient.getList(key);
        return rList.add(data);
    }

    /**
     * 为列表添加监听器
     *
     * @param key      键名
     * @param listener 监听器
     */
    public <T> void addListListener(final String key, final ObjectListener listener) {
        RList<T> rList = redissonClient.getList(key);
        rList.addListener(listener);
    }

    /**
     * 获取缓存的列表
     *
     * @param key 键名
     * @return 缓存的列表
     */
    public <T> List<T> getList(final String key) {
        RList<T> rList = redissonClient.getList(key);
        return rList.readAll();
    }

    /**
     * 获取缓存的列表
     *
     * @param key  键名
     * @param form 开始索引（包含）
     * @param to   结束索引（不包含）
     * @return 缓存的列表
     */
    public <T> List<T> getListRange(final String key, int form, int to) {
        RList<T> rList = redissonClient.getList(key);
        return rList.range(form, to);
    }

    /**
     * 缓存集合
     *
     * @param key     键名
     * @param dataSet 要添加到集合的数据集合
     * @return boolean
     */
    public <T> boolean setSet(final String key, final Set<T> dataSet) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.addAll(dataSet);
    }

    /**
     * 向集合添加一个元素
     *
     * @param key  键名
     * @param data 要添加的数据
     * @return boolean
     */
    public <T> boolean addSet(final String key, final T data) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.add(data);
    }

    /**
     * 为集合添加监听器
     *
     * @param key      键名
     * @param listener 监听器
     */
    public <T> void addSetListener(final String key, final ObjectListener listener) {
        RSet<T> rSet = redissonClient.getSet(key);
        rSet.addListener(listener);
    }

    /**
     * 获取缓存的集合
     *
     * @param key 键名
     * @return 缓存的集合
     */
    public <T> Set<T> getSet(final String key) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.readAll();
    }

    /**
     * 获取有序集合的值排名，按照从低到高的顺序排列。
     *
     * @param key    键名
     * @param object 对象
     * @param <T>    对象类型
     * @return 排名或如果值不存在则为 <code>null</code>
     */
    public <T> Integer getSortedSetRank(String key, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.rank(object);
    }

    /**
     * 获取有序集合的值排名，按照从高到低的顺序排列。
     *
     * @param key    键名
     * @param object 对象
     * @param <T>    对象类型
     * @return 排名或如果值不存在则为 <code>null</code>
     */
    public <T> Integer getSortedSetReRank(String key, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.revRank(object);
    }

    /**
     * 获取有序集合的元素分数，如果元素不存在，则返回 <code>null</code>。
     *
     * @param key     键名
     * @param element 元素
     * @param <T>     对象类型
     * @return 分数
     */
    public <T> Double getSortedSetScore(String key, T element) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.getScore(element);
    }

    /**
     * 获取有序集合的元素分数
     *
     * @param key      键名
     * @param elements 元素列表
     * @param <T>      对象类型
     * @return 元素分数列表
     */
    public <T> List<Double> getSortedSetScore(String key, List<T> elements) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.getScore(elements);
    }

    /**
     * 如果指定元素存在，则从有序集合中删除
     *
     * @param key     键名
     * @param element 要从排序集合中删除的元素（如果存在）
     * @param <T>     对象类型
     * @return 如果元素被删除，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public <T> boolean removeSortedSet(String key, T element) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.remove(element);
    }

    /**
     * 将指定 Map 中包含的所有元素添加到有序集合中。Map 中的键为对象，值为对应的分数。
     *
     * @param key     键名
     * @param objects 要添加的元素 Map
     * @param <T>     对象类型
     * @return 添加的元素数量，不包括有序集合中已存在的元素
     */
    public <T> int addSortedSet(String key, Map<T, Double> objects) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.addAll(objects);
    }

    /**
     * 将元素添加到有序集合中，如果它已经被添加过，则覆盖之前的分数。
     *
     * @param key    键名
     * @param score  对象分数
     * @param object 对象本身
     * @param <T>    对象类型
     * @return 如果元素被添加，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    public <T> boolean addSortedSet(String key, double score, T object) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.add(score, object);
    }

    /**
     * 按排名范围返回有序集合的值。索引是从零开始的。
     * <code>-1</code> 表示最高分，<code>-2</code> 表示第二高分。
     *
     * @param key        键名
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param <T>        对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.valueRange(startIndex, endIndex);
    }

    /**
     * 按分数范围返回有序集合的值
     *
     * @param key                 键名
     * @param startScore          开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param startScoreInclusive 是否包含开始分数
     * @param endScore            结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScoreInclusive   是否包含结束分数
     * @param <T>                 对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRange(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.valueRange(startScore, startScoreInclusive, endScore, endScoreInclusive);
    }

    /**
     * 按分数范围返回有序集合的值。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRange(String key, double startScore, double endScore) {
        return getSortedSetValueRange(key, startScore, true, endScore, true);
    }

    /**
     * 按排名范围返回有序集合的值，降序方式。索引是从零开始的。
     * <code>-1</code> 表示最高分，<code>-2</code> 表示第二高分。
     *
     * @param key        键名
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param <T>        对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.valueRangeReversed(startIndex, endIndex);
    }

    /**
     * 按分数范围返回有序集合的值，降序方式
     *
     * @param key                 键名
     * @param startScore          开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param startScoreInclusive 是否包含开始分数
     * @param endScore            结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScoreInclusive   是否包含结束分数
     * @param <T>                 对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRangeReversed(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.valueRangeReversed(startScore, startScoreInclusive, endScore, endScoreInclusive);
    }

    /**
     * 按分数范围返回有序集合的值，降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 元素列表
     */
    public <T> Collection<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore) {
        return getSortedSetValueRangeReversed(key, startScore, true, endScore, true);
    }

    /**
     * 按排名范围返回有序集合的条目（值及其分数）。索引是从零开始的。
     * <code>-1</code> 表示最高分，<code>-2</code> 表示第二高分。
     *
     * @param key        键名
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param <T>        对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.entryRange(startIndex, endIndex);
    }

    /**
     * 按分数范围返回有序集合的条目（值及其分数）。
     *
     * @param key                 键名
     * @param startScore          开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param startScoreInclusive 是否包含开始分数
     * @param endScore            结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScoreInclusive   是否包含结束分数
     * @param <T>                 对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRange(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.entryRange(startScore, startScoreInclusive, endScore, endScoreInclusive);
    }

    /**
     * 按分数范围返回有序集合的条目（值及其分数）。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore) {
        return getSortedSetEntryRange(key, startScore, true, endScore, true);
    }

    /**
     * 按排名范围返回有序集合的条目（值及其分数），降序方式。索引是从零开始的。
     * <code>-1</code> 表示最高分，<code>-2</code> 表示第二高分。
     *
     * @param key        键名
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param <T>        对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.entryRangeReversed(startIndex, endIndex);
    }

    /**
     * 按分数范围返回有序集合的条目（值及其分数），降序方式。
     *
     * @param key                 键名
     * @param startScore          开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param startScoreInclusive 是否包含开始分数
     * @param endScore            结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScoreInclusive   是否包含结束分数
     * @param <T>                 对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive) {
        RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        return scoredSortedSet.entryRangeReversed(startScore, startScoreInclusive, endScore, endScoreInclusive);
    }

    /**
     * 按分数范围返回有序集合的条目（值及其分数），降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 条目列表
     */
    public <T> Collection<ScoredEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore) {
        return getSortedSetEntryRange(key, startScore, true, endScore, true);
    }

    /**
     * 缓存哈希表
     *
     * @param key     键名
     * @param dataMap 要设置的键值对映射
     */
    public <K, V> void setMap(final String key, final Map<K, V> dataMap) {
        if (dataMap != null) {
            RMap<K, V> rMap = redissonClient.getMap(key);
            rMap.putAll(dataMap);
        }
    }

    /**
     * 为哈希表添加监听器
     *
     * @param key      键名
     * @param listener 监听器
     */
    public <T> void addMapListener(final String key, final ObjectListener listener) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        rMap.addListener(listener);
    }

    /**
     * 获取缓存的哈希表
     *
     * @param key 键名
     * @return 键值对映射
     */
    public <T> Map<String, T> getMap(final String key) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    /**
     * 获取缓存的哈希表的所有键
     *
     * @param key 键名
     * @return 键集合
     */
    public <T> Set<String> getMapKeySet(final String key) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.keySet();
    }

    /**
     * 缓存哈希表中的值
     *
     * @param key   键名
     * @param hKey  哈希表中的键
     * @param value 要缓存的值
     */
    public <T> void setMapValue(final String key, final String hKey, final T value) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        rMap.put(hKey, value);
    }

    /**
     * 获取缓存的哈希表中的值
     *
     * @param key  键名
     * @param hKey 哈希表中的键
     * @return 对应的值
     */
    public <T> T getMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.get(hKey);
    }

    /**
     * 删除哈希表中的键值对
     *
     * @param key  键名
     * @param hKey 哈希表中的键
     * @return 被删除的值
     */
    public <T> T deleteMapValue(final String key, final String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.remove(hKey);
    }

    /**
     * 删除哈希表中的键值对
     *
     * @param key   键名
     * @param hKeys 键值对映射
     */
    public <T> void deleteMapValue(final String key, final Set<String> hKeys) {
        RBatch batch = redissonClient.createBatch();
        RMapAsync<String, T> rMap = batch.getMap(key);
        for (String hKey : hKeys) {
            rMap.removeAsync(hKey);
        }
        batch.execute();
    }

    /**
     * 获取缓存的哈希表中的值
     *
     * @param key   键名
     * @param hKeys 要获取的哈希表键集合
     * @return 所有键值对的 Map
     */
    public <K, V> Map<K, V> getMapValue(final String key, final Set<K> hKeys) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        return rMap.getAll(hKeys);
    }

    /**
     * 设置原子值
     *
     * @param key   键名
     * @param value 要设置的原子值
     */
    public void setAtomicValue(String key, long value) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        atomic.set(value);
    }

    /**
     * 获取原子值
     *
     * @param key 键名
     * @return 当前原子值
     */
    public long getAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.get();
    }

    /**
     * 递增原子值
     *
     * @param key 键名
     * @return 递增后的原子值
     */
    public long incrementAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.incrementAndGet();
    }

    /**
     * 递减原子值
     *
     * @param key 键名
     * @return 递减后的原子值
     */
    public long decrementAtomicValue(String key) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key);
        return atomic.decrementAndGet();
    }

    /**
     * 获取指定前缀的键
     *
     * @param pattern 字符串前缀
     * @return 匹配的键集合
     */
    public Collection<String> getKeys(final String pattern) {
        KeysScanOptions keysScanOptions = KeysScanOptions.defaults().pattern(pattern);
        Iterable<String> keys = redissonClient.getKeys().getKeys(keysScanOptions);
        return StreamSupport.stream(keys.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * 删除指定前缀的键
     *
     * @param pattern 字符串前缀
     */
    public void deleteKeys(final String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    /**
     * 检查 Redis 中是否存在指定的键
     *
     * @param key 键名
     * @return boolean
     */
    public Boolean hasKey(String key) {
        RKeys rKeys = redissonClient.getKeys();
        return rKeys.countExists(key) > 0;
    }
}
