package cn.floseek.fastcache.cache.serializer;

/**
 * 序列化器接口
 * <p>
 * 该接口定义了通用的序列化和反序列化方法，用于将对象序列化为字节数组，以及将字节数组反序列化为对象
 * </p>
 *
 * @author ChenHongwei472
 */
public interface Serializer {

    /**
     * 序列化
     *
     * @param object 对象
     * @param <T>    对象类型
     * @return 字节数组
     */
    <T> byte[] serialize(T object);

    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @param <T>   对象类型
     * @return 对象
     */
    <T> T deserialize(byte[] bytes);
}
