package cn.floseek.fastcache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存消息
 *
 * @param <K> 缓存键类型
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheMessage<K> {

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存键
     */
    private K key;

    /**
     * 实例 ID
     */
    private String instanceId;
}
