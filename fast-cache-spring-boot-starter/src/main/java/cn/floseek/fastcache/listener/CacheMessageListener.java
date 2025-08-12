package cn.floseek.fastcache.listener;

import cn.floseek.fastcache.manager.broadcast.BroadcastManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * 缓存消息监听器
 *
 * @author ChenHongwei472
 */
public class CacheMessageListener {

    private final BroadcastManager broadcastManager;

    public CacheMessageListener(BroadcastManager broadcastManager) {
        this.broadcastManager = broadcastManager;
    }

    @PostConstruct
    public void init() {
        broadcastManager.startSubscribe();
    }

    @PreDestroy
    public void destroy() {
        broadcastManager.close();
    }
}
