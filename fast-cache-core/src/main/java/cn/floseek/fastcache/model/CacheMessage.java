package cn.floseek.fastcache.model;

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
     * 缓存键集合
     */
    private List<Object> keys;
}
