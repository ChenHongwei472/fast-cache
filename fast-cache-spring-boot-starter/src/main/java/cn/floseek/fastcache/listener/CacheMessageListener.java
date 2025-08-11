package cn.floseek.fastcache.listener;

import cn.floseek.fastcache.manager.LocalCacheManager;
import cn.floseek.fastcache.service.broadcast.BroadcastService;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 缓存消息监听器
 *
 * @author ChenHongwei472
 */
public class CacheMessageListener implements ApplicationRunner {

    private final BroadcastService broadcastService;

    public CacheMessageListener(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        broadcastService.listen(cacheMessage -> {
            Cache<Object, Object> cache = LocalCacheManager.getInstance().getCache(cacheMessage.getCacheName());
            if (cache != null) {
                cache.invalidateAll(cacheMessage.getKeys());
            }
        });
    }
}
