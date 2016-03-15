package buildcraft.core.lib.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

/** An adding {@link IModelCache} that takes a single key and adds all of the sub-keys given by the
 * {@link IModelKeyMultipleSameMapper}
 * 
 * @author AlexIIL
 * @creation-date 14 Mar 2016
 *
 * @param <K> The from type. This is the input key type.
 * @param <T> The "to" type. This is the output key type. */
public class ModelCacheMultipleSame<K, T> implements IModelCache<K> {
    private final IModelCache<K> mainCache;
    private final IModelKeyMultipleSameMapper<K, T> mapper;
    private final IModelCache<T> seperateCache;

    public ModelCacheMultipleSame(String mainName, IModelKeyMultipleSameMapper<K, T> mapper, IModelCache<T> seperateCache) {
        this.mainCache = new ModelCache<>(mainName, this::load);
        this.mapper = mapper;
        this.seperateCache = seperateCache;
    }

    private List<MutableQuad> load(K key) {
        List<MutableQuad> quads = new ArrayList<>();
        for (T to : mapper.map(key)) {
            seperateCache.appendAsMutable(to, quads);
        }
        return quads;
    }

    @Override
    public void appendAsMutable(K key, List<MutableQuad> quads) {
        mainCache.appendAsMutable(key, quads);
    }

    @Override
    public ImmutableList<BakedQuad> bake(K key, VertexFormat format) {
        return mainCache.bake(key, format);
    }

    @Override
    public void render(K key, WorldRenderer wr) {
        mainCache.render(key, wr);
    }

    @Override
    public void renderDisplayList(K key) {
        for (T to : mapper.map(key)) {
            seperateCache.renderDisplayList(to);
        }
    }

    public interface IModelKeyMultipleSameMapper<F, T> {
        Collection<T> map(F key);
    }
}
