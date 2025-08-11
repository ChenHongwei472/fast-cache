package cn.floseek.fastcache.service.broadcast;

import cn.floseek.fastcache.model.CacheMessage;
import cn.floseek.fastcache.service.redis.RedisService;
import org.redisson.api.RTopic;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Redis 广播服务实现
 *
 * @author ChenHongwei472
 */
public class RedisBroadcastService implements BroadcastService {

    private final RedisService redisService;
    private final String instanceId;
    private final String topicName;

    public RedisBroadcastService(RedisService redisService, String topicName) {
        this.redisService = redisService;
        this.instanceId = UUID.randomUUID().toString();
        this.topicName = topicName;
    }

    @Override
    public void broadcast(String cacheName, Object key) {
        CacheMessage<Object> message = CacheMessage.builder()
                .cacheName(cacheName)
                .key(key)
                .instanceId(instanceId)
                .build();

        RTopic topic = redisService.getTopic(topicName);
        topic.publish(message);
    }

    @Override
    public void listen(Consumer<CacheMessage<?>> handler) {
        RTopic topic = redisService.getTopic(topicName);
        topic.addListener(CacheMessage.class, (channel, msg) -> {
            if (!msg.getInstanceId().equals(instanceId)) {
                handler.accept(msg);
            }
        });
    }
}
