package cn.floseek.fastcache.core.bloomfilter;

import java.util.Collection;
import java.util.List;

/**
 * 布隆过滤器接口
 *
 * @author ChenHongwei472
 */
public interface BloomFilter {

    /**
     * 添加元素
     *
     * @param object 要添加的元素
     * @return boolean
     */
    boolean add(String object);

    /**
     * 添加元素
     *
     * @param elements 要添加的元素
     * @return 添加元素的数量
     */
    long add(Collection<String> elements);

    /**
     * 判断元素是否可能存在
     *
     * @param object 元素
     * @return boolean
     */
    boolean mightContain(String object);

    /**
     * 重建
     *
     * @param dataList 数据列表
     */
    void rebuild(List<String> dataList);
}
