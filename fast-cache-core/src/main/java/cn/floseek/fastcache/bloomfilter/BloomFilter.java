package cn.floseek.fastcache.bloomfilter;

import java.util.Collection;
import java.util.List;

/**
 * 布隆过滤器接口
 * <p>
 * 定义布隆过滤器的基本操作，包括元素添加、存在性判断和布隆过滤器重建等功能
 * </p>
 *
 * @author ChenHongwei472
 */
public interface BloomFilter {

    /**
     * 添加单个元素
     *
     * @param object 待添加元素
     * @return boolean
     */
    boolean add(String object);

    /**
     * 批量添加元素
     *
     * @param elements 待添加元素集合
     * @return 添加元素的数量
     */
    long add(Collection<String> elements);

    /**
     * 判断元素是否可能存在
     *
     * @param object 待检测元素
     * @return boolean
     */
    boolean mightContain(String object);

    /**
     * 重建布隆过滤器
     *
     * @param dataList 数据列表
     */
    void rebuild(List<String> dataList);
}
