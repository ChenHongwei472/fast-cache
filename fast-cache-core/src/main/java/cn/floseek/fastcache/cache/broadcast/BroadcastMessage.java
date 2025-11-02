package cn.floseek.fastcache.cache.broadcast;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 广播消息
 * <p>
 * 用于在分布式环境中多个实例之间的本地缓存数据同步
 * </p>
 *
 * @author ChenHongwei472
 */
public class BroadcastMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 更新
     */
    public static final int TYPE_UPDATE = 1;
    /**
     * 删除
     */
    public static final int TYPE_INVALIDATE = 2;

    /**
     * 来源实例 ID
     */
    private String instanceId;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 类型
     */
    private int type;

    /**
     * 缓存键值对
     * <p>
     * 当 {@link #type} = {@link #TYPE_UPDATE} 时有效，用于批量修改缓存
     * </p>
     */
    private Map<Object, Object> keyValues;

    /**
     * 缓存键列表
     * <p>
     * 当 {@link #type} = {@link #TYPE_INVALIDATE} 时有效，用于批量删除缓存
     * </p>
     */
    private List<Object> keys;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<Object, Object> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<Object, Object> keyValues) {
        this.keyValues = keyValues;
    }

    public List<Object> getKeys() {
        return keys;
    }

    public void setKeys(List<Object> keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "BroadcastMessage{" +
                "instanceId='" + instanceId + '\'' +
                ", cacheName='" + cacheName + '\'' +
                ", type=" + type +
                ", keyValues=" + keyValues +
                ", keys=" + keys +
                '}';
    }

    /**
     * 创建删除广播消息
     *
     * @param instanceId 实例 ID
     * @param cacheName  缓存名称
     * @param keys       缓存键列表
     * @return {@link BroadcastMessage}
     */
    public static BroadcastMessage buildInvalidate(String instanceId, String cacheName, List<Object> keys) {
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setInstanceId(instanceId);
        broadcastMessage.setCacheName(cacheName);
        broadcastMessage.setType(TYPE_INVALIDATE);
        broadcastMessage.setKeys(keys);
        return broadcastMessage;
    }

    /**
     * 创建更新广播消息
     *
     * @param instanceId 实例 ID
     * @param cacheName  缓存名称
     * @param keyValues  缓存键值对
     * @return {@link BroadcastMessage}
     */
    public static BroadcastMessage buildUpdate(String instanceId, String cacheName, Map<Object, Object> keyValues) {
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setInstanceId(instanceId);
        broadcastMessage.setCacheName(cacheName);
        broadcastMessage.setType(TYPE_UPDATE);
        broadcastMessage.setKeyValues(keyValues);
        return broadcastMessage;
    }

    /**
     * 判断是否为删除广播消息
     *
     * @return boolean
     */
    public boolean isInvalidate() {
        return this.type == TYPE_INVALIDATE;
    }

    /**
     * 判断是否为更新广播消息
     *
     * @return boolean
     */
    public boolean isUpdate() {
        return this.type == TYPE_UPDATE;
    }

}
