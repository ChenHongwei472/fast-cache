package cn.floseek.fastcache.service.broadcast;

import cn.floseek.fastcache.model.CacheMessage;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Redis 广播服务实现
 *
 * @author ChenHongwei472
 */
public class RedisBroadcastService implements BroadcastService {

    private final RedissonClient redissonClient;
    private final String instanceId;
    private final String topicName;

    public RedisBroadcastService(RedissonClient redissonClient, String topicName) {
        this.redissonClient = redissonClient;
        this.instanceId = UUID.randomUUID().toString();
        this.topicName = topicName;
    }

    @Override
    public <K> void broadcast(String cacheName, K key) {
        broadcast(cacheName, List.of(key));
    }

    @Override
    public <K> void broadcast(String cacheName, Collection<? extends K> keys) {
        CacheMessage<Object> message = CacheMessage.builder()
                .cacheName(cacheName)
                .keys(keys)
                .instanceId(instanceId)
                .build();

        RTopic topic = redissonClient.getTopic(topicName);
        topic.publish(message);
    }

    @Override
    public void listen(Consumer<CacheMessage<?>> handler) {
        RTopic topic = redissonClient.getTopic(topicName);
        topic.addListener(CacheMessage.class, (channel, msg) -> {
            if (!msg.getInstanceId().equals(instanceId)) {
                handler.accept(msg);
            }
        });
    }
}
