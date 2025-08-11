package cn.floseek.fastcache.service.broadcast;

import cn.floseek.fastcache.model.CacheMessage;

import java.util.function.Consumer;

/**
 * 广播服务接口
 *
 * @author ChenHongwei472
 */
public interface BroadcastService {

    /**
     * 广播消息
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    void broadcast(String cacheName, Object key);

    /**
     * 监听消息
     *
     * @param handler 消息处理器
     */
    void listen(Consumer<CacheMessage<?>> handler);
}
