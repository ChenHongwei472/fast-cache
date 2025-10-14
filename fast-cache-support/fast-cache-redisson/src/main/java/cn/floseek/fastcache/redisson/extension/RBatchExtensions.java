package cn.floseek.fastcache.redisson.extension;

import cn.floseek.fastcache.cache.converter.KeyConverter;
import org.redisson.api.RBatch;
import org.redisson.api.RBucketAsync;
import org.redisson.client.codec.ByteArrayCodec;

/**
 * RBatch 扩展方法
 *
 * @author ChenHongwei472
 */
public class RBatchExtensions {

    public static RBucketAsync<byte[]> getByteArrayBucket(RBatch batch, String key, KeyConverter keyConverter) {
        return batch.getBucket(keyConverter.convert(key), ByteArrayCodec.INSTANCE);
    }

}
