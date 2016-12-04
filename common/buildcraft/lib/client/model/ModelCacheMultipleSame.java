package buildcraft.lib.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

/** An adding {@link IModelCache} that takes a single key and adds all of the sub-keys given by the
 * {@link IModelKeyMultipleSameMapper}
 * 
 * @author AlexIIL
 * @since 14 Mar 2016
 *
 * @param <K> The from type. This is the input key type.
 * @param <T> The "to" type. This is the output key type. */
public class ModelCacheMultipleSame<K, T> implements IModelCache<K> {
    private final IModelCache<K> mainCache;
    private final IModelKeyMultipleSameMapper<K, T> mapper;
    private final IModelCache<T> seperateCache;

    public ModelCacheMultipleSame(IModelKeyMultipleSameMapper<K, T> mapper, IModelCache<T> seperateCache) {
        this.mainCache = new ModelCache<>(this::load);
        this.mapper = mapper;
        this.seperateCache = seperateCache;
    }

    private List<BakedQuad> load(K key) {
        List<BakedQuad> quads = new ArrayList<>();
        for (T to : mapper.map(key)) {
            quads.addAll(seperateCache.bake(to));
        }
        return quads;
    }

    @Override
    public List<BakedQuad> bake(K key) {
        if (ModelCache.cacheJoined) {
            return mainCache.bake(key);
        } else {
            return load(key);
        }
    }

    @Override
    public void clear() {
        mainCache.clear();
        seperateCache.clear();
    }

    public interface IModelKeyMultipleSameMapper<F, T> {
        Collection<T> map(F key);
    }
}
