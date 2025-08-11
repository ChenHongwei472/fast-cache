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
    private final LocalCacheManager localCacheManager;

    public CacheMessageListener(BroadcastService broadcastService, LocalCacheManager localCacheManager) {
        this.broadcastService = broadcastService;
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        broadcastService.listen(cacheMessage -> {
            Cache<Object, Object> cache = localCacheManager.getCache(cacheMessage.getCacheName());
            if (cache != null) {
                cache.invalidate(cacheMessage.getKey());
            }
        });
    }
}
