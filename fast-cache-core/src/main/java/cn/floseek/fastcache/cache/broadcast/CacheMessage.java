package cn.floseek.fastcache.cache.broadcast;

import cn.floseek.fastcache.cache.config.CacheType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 缓存消息
 *
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实例 ID
     */
    private String instanceId;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存类型
     */
    private CacheType cacheType;

    /**
     * 缓存键列表
     */
    private List<Object> keys;
}
