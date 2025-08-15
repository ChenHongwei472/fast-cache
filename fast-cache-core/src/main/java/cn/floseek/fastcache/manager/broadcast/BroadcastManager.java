package cn.floseek.fastcache.manager.broadcast;

import cn.floseek.fastcache.model.CacheMessage;

/**
 * 广播管理器接口
 *
 * @author ChenHongwei472
 */
public interface BroadcastManager {

    /**
     * 发布消息
     *
     * @param cacheMessage 缓存消息
     */
    void publish(CacheMessage cacheMessage);

    /**
     * 启动订阅
     */
    void startSubscribe();

    /**
     * 关闭
     */
    void close();

    /**
     * 获取实例 ID
     *
     * @return 实例 ID
     */
    String getInstanceId();
}
