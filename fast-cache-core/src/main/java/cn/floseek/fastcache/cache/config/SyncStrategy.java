package cn.floseek.fastcache.cache.config;

/**
 * 缓存同步策略
 * <p>
 * 定义在分布式环境中本地缓存数据的同步方式，用于控制多个节点间本地缓存的数据一致性
 * </p>
 *
 * @author ChenHongwei472
 */
public enum SyncStrategy {
    /**
     * 不同步
     */
    NONE,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    INVALIDATE
}
