package cn.floseek.fastcache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

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
     * 缓存键集合
     */
    private Collection<? extends K> keys;

    /**
     * 实例 ID
     */
    private String instanceId;
}
