package buildcraft.lib.client.model;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.client.renderer.block.model.BakedQuad;

/** Implements a caching system for models with potentially infinite variants. Automatically expires entries after a
 * configurable time period, and up to a maximum number. */
public class ModelCache<K> implements IModelCache<K> {
    public static boolean cacheJoined = false;

    private final LoadingCache<K, List<BakedQuad>> modelCache;

    public ModelCache(IModelGenerator<K> generator) {
        modelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(CacheLoader.from(generator::generate));
    }

    @Override
    public List<BakedQuad> bake(K key) {
        return modelCache.getUnchecked(key);
    }

    @Override
    public void clear() {
        modelCache.invalidateAll();
    }

    public interface IModelGenerator<T> {
        List<BakedQuad> generate(T key);
    }
}
