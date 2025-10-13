package cn.floseek.fastcache.redis;

/**
 * 有序集合条目
 *
 * @param <V>   值类型
 * @param score 分数
 * @param value 值
 * @author ChenHongwei472
 */
public record SortedEntry<V>(Double score, V value) implements Comparable<SortedEntry<V>> {

    @Override
    public int compareTo(SortedEntry<V> o) {
        return score.compareTo(o.score);
    }

}
