package cn.floseek.fastcache.cache.broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 广播消息
 * <p>
 * 用于在分布式环境中多个实例之间的本地缓存数据同步
 * </p>
 *
 * @author ChenHongwei472
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 来源实例 ID
     */
    private String instanceId;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 缓存键列表
     */
    private List<Object> keys;
}
