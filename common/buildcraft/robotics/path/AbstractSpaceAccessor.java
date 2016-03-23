package buildcraft.robotics.path;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractSpaceAccessor<K> implements IVirtualSpaceAccessor<K> {
    private final LoadingCache<K, IVirtualPoint<K>> pointCache = CacheBuilder.newBuilder().build(CacheLoader.from(this::loadPoint));

    public abstract IVirtualPoint<K> loadPoint(K key);

    @Override
    public IVirtualPoint<K> getPoint(K key) {
        return pointCache.getUnchecked(key);
    }
}
