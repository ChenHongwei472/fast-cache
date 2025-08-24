package cn.floseek.fastcache.cache.decorator;

import cn.floseek.fastcache.cache.Cache;
import cn.floseek.fastcache.cache.config.CacheLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 缓存加载器装饰器
 * <p>
 * 主要用于在缓存未命中时，调用配置的缓存加载器加载缓存值
 * </p>
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author ChenHongwei472
 */
public class CacheLoaderDecorator<K, V> extends CacheDecorator<K, V> {

    private final CacheLoader<K, V> loader;
    private final boolean loaderEnabled;

    public CacheLoaderDecorator(Cache<K, V> decoratedCache) {
        super(decoratedCache);
        this.loader = decoratedCache.getConfig().getLoader();
        this.loaderEnabled = decoratedCache.getConfig().loaderEnabled();
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (Objects.nonNull(value)) {
            return value;
        }

        if (loaderEnabled) {
            value = loader.load(key);
            if (Objects.nonNull(value)) {
                super.put(key, value);
            }
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Map<K, V> valueMap = super.getAll(keys);
        if (MapUtils.isEmpty(valueMap)) {
            return Collections.emptyMap();
        }

        // 缓存结果
        Map<K, V> resultMap = new HashMap<>(valueMap);
        // 未命中的键
        Set<K> missingKeys = new HashSet<>(keys);

        // 移除命中的键
        missingKeys.removeAll(valueMap.keySet());

        // 如果有未命中的键，则尝试从分布式缓存中获取数据
        if (CollectionUtils.isNotEmpty(missingKeys)) {
            if (loaderEnabled) {
                Map<K, V> kvMap = loader.loadAll(missingKeys);
                // 如果获取到数据，则添加到缓存结果中，并回填到缓存中
                if (MapUtils.isNotEmpty(kvMap)) {
                    resultMap.putAll(kvMap);
                    super.putAll(kvMap);
                }
            }
        }

        return resultMap;
    }
}
