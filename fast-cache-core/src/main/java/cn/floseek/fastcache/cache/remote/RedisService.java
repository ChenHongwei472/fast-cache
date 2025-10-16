package cn.floseek.fastcache.cache.remote;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 服务接口
 *
 * @author ChenHongwei472
 */
public interface RedisService {

    /**
     * 缓存对象
     *
     * @param key   键名
     * @param value 要缓存的对象
     */
    <T> void setObject(String key, T value);

    /**
     * 缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     */
    <T> void setObject(String key, T value, Duration duration);

    /**
     * 批量缓存对象
     *
     * @param objects 要缓存的对象 Map
     */
    <T> void setObjects(Map<String, T> objects);

    /**
     * 批量缓存对象
     *
     * @param objects  要缓存的对象 Map
     * @param duration 过期时间
     */
    <T> void setObjects(Map<String, T> objects, Duration duration);

    /**
     * 如果键不存在，则缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     * @return boolean
     */
    <T> boolean setObjectIfAbsent(String key, T value, Duration duration);

    /**
     * 如果键已存在，则缓存对象
     *
     * @param key      键名
     * @param value    要缓存的对象
     * @param duration 过期时间
     * @return boolean
     */
    <T> boolean setObjectIfExists(String key, T value, Duration duration);

    /**
     * 设置键的过期时间
     *
     * @param key      键名
     * @param duration 过期时间
     * @return boolean
     */
    boolean expire(String key, Duration duration);

    /**
     * 获取缓存的对象
     *
     * @param key 键名
     * @return 缓存的对象
     */
    <T> T getObject(String key);

    /**
     * 批量获取缓存的对象
     *
     * @param keys 键名的集合
     * @param <T>  对象类型
     * @return 缓存的对象集合
     */
    <T> List<T> getObjects(Collection<String> keys);

    /**
     * 获取键的剩余存活时间
     *
     * @param key 键名
     * @return 剩余存活时间
     */
    long getTimeToLive(String key);

    /**
     * 删除缓存的对象
     *
     * @param key 键名
     * @return boolean
     */
    boolean deleteObject(String key);

    /**
     * 删除缓存的对象
     *
     * @param keys 键名的集合
     */
    void deleteObjects(Collection<String> keys);

    /**
     * 检查缓存的对象是否存在
     *
     * @param key 键名
     * @return boolean
     */
    boolean existsObject(String key);

    /**
     * 缓存列表
     *
     * @param key      键名
     * @param dataList 要添加到列表的数据列表
     * @return boolean
     */
    <T> boolean setList(String key, List<T> dataList);

    /**
     * 向列表添加一个元素
     *
     * @param key  键名
     * @param data 要添加的数据
     * @return boolean
     */
    <T> boolean addList(String key, T data);

    /**
     * 获取缓存的列表
     *
     * @param key 键名
     * @return 缓存的列表
     */
    <T> List<T> getList(String key);

    /**
     * 获取缓存的列表
     *
     * @param key  键名
     * @param form 开始索引（包含）
     * @param to   结束索引（不包含）
     * @return 缓存的列表
     */
    <T> List<T> getListRange(String key, int form, int to);

    /**
     * 缓存集合
     *
     * @param key     键名
     * @param dataSet 要添加到集合的数据集合
     * @return boolean
     */
    <T> boolean setSet(String key, Set<T> dataSet);

    /**
     * 向集合添加一个元素
     *
     * @param key  键名
     * @param data 要添加的数据
     * @return boolean
     */
    <T> boolean addSet(String key, T data);

    /**
     * 获取缓存的集合
     *
     * @param key 键名
     * @return 缓存的集合
     */
    <T> Set<T> getSet(String key);

    /**
     * 获取有序集合的值排名，按照从低到高的顺序排列。
     *
     * @param key    键名
     * @param object 对象
     * @param <T>    对象类型
     * @return 排名或如果值不存在则为 <code>null</code>
     */
    <T> Integer getSortedSetRank(String key, T object);

    /**
     * 获取有序集合的值排名，按照从高到低的顺序排列。
     *
     * @param key    键名
     * @param object 对象
     * @param <T>    对象类型
     * @return 排名或如果值不存在则为 <code>null</code>
     */
    <T> Integer getSortedSetReRank(String key, T object);

    /**
     * 获取有序集合的元素分数，如果元素不存在，则返回 <code>null</code>。
     *
     * @param key     键名
     * @param element 元素
     * @param <T>     对象类型
     * @return 分数
     */
    <T> Double getSortedSetScore(String key, T element);

    /**
     * 获取有序集合的元素分数
     *
     * @param key      键名
     * @param elements 元素列表
     * @param <T>      对象类型
     * @return 元素分数列表
     */
    <T> List<Double> getSortedSetScore(String key, List<T> elements);

    /**
     * 如果指定元素存在，则从有序集合中删除
     *
     * @param key     键名
     * @param element 要从排序集合中删除的元素（如果存在）
     * @param <T>     对象类型
     * @return 如果元素被删除，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    <T> boolean removeSortedSet(String key, T element);

    /**
     * 将指定 Map 中包含的所有元素添加到有序集合中。Map 中的键为对象，值为对应的分数。
     *
     * @param key     键名
     * @param objects 要添加的元素 Map
     * @param <T>     对象类型
     * @return 添加的元素数量，不包括有序集合中已存在的元素
     */
    <T> int addSortedSet(String key, Map<T, Double> objects);

    /**
     * 将指定 Map 中包含的所有元素添加到有序集合中。Map 中的键为对象，值为对应的分数。
     *
     * @param key      键名
     * @param objects  要添加的元素 Map
     * @param duration 过期时间
     * @param <T>      对象类型
     * @return 添加的元素数量，不包括有序集合中已存在的元素
     */
    <T> int addSortedSet(String key, Map<T, Double> objects, Duration duration);

    /**
     * 将元素添加到有序集合中，如果它已经被添加过，则覆盖之前的分数。
     *
     * @param key    键名
     * @param score  对象分数
     * @param object 对象本身
     * @param <T>    对象类型
     * @return 如果元素被添加，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    <T> boolean addSortedSet(String key, double score, T object);

    /**
     * 将元素添加到有序集合中，如果它已经被添加过，则覆盖之前的分数。
     *
     * @param key      键名
     * @param score    对象分数
     * @param object   对象本身
     * @param duration 过期时间
     * @param <T>      对象类型
     * @return 如果元素被添加，则返回 <code>true</code>，否则返回 <code>false</code>
     */
    <T> boolean addSortedSet(String key, double score, T object, Duration duration);

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
    <T> List<T> getSortedSetValueRange(String key, int startIndex, int endIndex);

    /**
     * 按分数范围返回有序集合的值。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 元素列表
     */
    <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore);

    /**
     * 按分数范围返回有序集合的值。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param offset     排序数据的偏移量
     * @param count      排序数据的数量
     * @param <T>        对象类型
     * @return 元素列表
     */
    <T> List<T> getSortedSetValueRange(String key, double startScore, double endScore, int offset, int count);

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
    <T> List<T> getSortedSetValueRangeReversed(String key, int startIndex, int endIndex);

    /**
     * 按分数范围返回有序集合的值，降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 元素列表
     */
    <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore);

    /**
     * 按分数范围返回有序集合的值，降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param offset     排序数据的偏移量
     * @param count      排序数据的数量
     * @param <T>        对象类型
     * @return 元素列表
     */
    <T> List<T> getSortedSetValueRangeReversed(String key, double startScore, double endScore, int offset, int count);

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
    <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, int startIndex, int endIndex);

    /**
     * 按分数范围返回有序集合的条目（值及其分数）。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 条目列表
     */
    <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore);

    /**
     * 按分数范围返回有序集合的条目（值及其分数）。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param offset     排序数据的偏移量
     * @param count      排序数据的数量
     * @param <T>        对象类型
     * @return 条目列表
     */
    <T> List<SortedEntry<T>> getSortedSetEntryRange(String key, double startScore, double endScore, int offset, int count);

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
    <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, int startIndex, int endIndex);

    /**
     * 按分数范围返回有序集合的条目（值及其分数），降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param <T>        对象类型
     * @return 条目列表
     */
    <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore);

    /**
     * 按分数范围返回有序集合的条目（值及其分数），降序方式。分数范围包含开始分数和结束分数。
     *
     * @param key        键名
     * @param startScore 开始分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param endScore   结束分数。使用 <code>Double.POSITIVE_INFINITY</code> 或 <code>Double.NEGATIVE_INFINITY</code> 定义无穷数
     * @param offset     排序数据的偏移量
     * @param count      排序数据的数量
     * @param <T>        对象类型
     * @return 条目列表
     */
    <T> List<SortedEntry<T>> getSortedSetEntryRangeReversed(String key, double startScore, double endScore, int offset, int count);

    /**
     * 设置原子值
     *
     * @param key   键名
     * @param value 要设置的原子值
     */
    void setAtomicValue(String key, long value);

    /**
     * 获取原子值
     *
     * @param key 键名
     * @return 当前原子值
     */
    long getAtomicValue(String key);

    /**
     * 递增原子值
     *
     * @param key 键名
     * @return 递增后的原子值
     */
    long incrementAtomicValue(String key);

    /**
     * 递减原子值
     *
     * @param key 键名
     * @return 递减后的原子值
     */
    long decrementAtomicValue(String key);

    /**
     * 获取指定前缀的键
     *
     * @param pattern 字符串前缀
     * @return 匹配的键集合
     */
    Collection<String> getKeys(String pattern);

    /**
     * 删除指定前缀的键
     *
     * @param pattern 字符串前缀
     */
    void deleteKeys(String pattern);

    /**
     * 检查 Redis 中是否存在指定的键
     *
     * @param key 键名
     * @return boolean
     */
    Boolean hasKey(String key);

}
