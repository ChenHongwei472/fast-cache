package cn.floseek.fastcache.redis;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 有序集合条目
 *
 * @param <V> 值类型
 * @author ChenHongwei472
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SortedEntry<V> implements Comparable<SortedEntry<V>> {

    /**
     * 分数
     */
    private final Double score;

    /**
     * 值
     */
    private final V value;

    @Override
    public int compareTo(SortedEntry<V> o) {
        return score.compareTo(o.score);
    }
}
