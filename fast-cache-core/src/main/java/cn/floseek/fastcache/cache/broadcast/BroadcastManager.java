package cn.floseek.fastcache.cache.broadcast;

/**
 * 广播管理器接口
 * <p>
 * 定义了缓存广播功能的核心方法，用于在分布式环境中同步缓存数据
 * </p>
 *
 * @author ChenHongwei472
 */
public interface BroadcastManager {

    /**
     * 发布广播消息
     *
     * @param broadcastMessage 广播消息对象
     */
    void publish(BroadcastMessage broadcastMessage);

    /**
     * 订阅广播频道
     */
    void subscribe();

    /**
     * 关闭广播管理器
     */
    void close();

    /**
     * 获取当前实例 ID
     *
     * @return 实例 ID
     */
    String getInstanceId();
}
