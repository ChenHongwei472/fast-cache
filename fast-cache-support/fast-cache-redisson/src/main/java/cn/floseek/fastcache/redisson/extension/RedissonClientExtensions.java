package cn.floseek.fastcache.redisson.extension;

import cn.floseek.fastcache.cache.converter.KeyConverter;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.ByteArrayCodec;

import java.util.function.Consumer;

/**
 * Redisson 客户端扩展方法
 *
 * @author ChenHongwei472
 */
public class RedissonClientExtensions {

    public static RBucket<byte[]> getByteArrayBucket(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getBucket(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    public static RList<byte[]> getByteArrayList(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getList(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    public static RSet<byte[]> getByteArraySet(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getSet(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    public static RScoredSortedSet<byte[]> getByteArrayScoredSortedSet(RedissonClient redissonClient, String key, KeyConverter keyConverter) {
        return redissonClient.getScoredSortedSet(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

    public static BatchResult<?> executeBatch(RedissonClient redissonClient, Consumer<RBatch> batchOperation) {
        RBatch batch = redissonClient.createBatch();
        batchOperation.accept(batch);
        return batch.execute();
    }

}
