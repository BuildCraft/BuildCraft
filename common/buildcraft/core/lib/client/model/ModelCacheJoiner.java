package buildcraft.core.lib.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

import buildcraft.core.lib.client.model.ModelCacheHelper.IModelGenerator;

/**
 * 
 * Created on 14 Mar 2016 by AlexIIL
 *
 * @param <K>
 */
public class ModelCacheJoiner<K> implements IModelCache<K> {
    private final IModelCache<K> mainCache;
    private final ImmutableList<ModelKeyWrapper<K, ?>> modelKeyWrappers;

    public ModelCacheJoiner(String mainName, List<ModelKeyWrapper<K, ?>> wrappers) {
        this.modelKeyWrappers = ImmutableList.copyOf(wrappers);
        this.mainCache = new ModelCacheHelper<>(mainName, this::load);
    }

    private List<MutableQuad> load(K key) {
        List<MutableQuad> quads = new ArrayList<>();
        for (ModelKeyWrapper<K, ?> wrapper : modelKeyWrappers) {
            wrapper.appendQuads(key, quads);
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

    public static class ModelKeyWrapper<K, T> {
        private final IModelKeyMapper<K, T> mapper;
        private final IModelCache<T> cache;

        public ModelKeyWrapper(String detailedName, IModelKeyMapper<K, T> mapper, IModelGenerator<T> generator) {
            this.mapper = mapper;
            this.cache = new ModelCacheHelper<>(detailedName, generator);
        }

        public ModelKeyWrapper(IModelKeyMapper<K, T> mapper, IModelCache<T> cache) {
            this.mapper = mapper;
            this.cache = cache;
        }

        public void appendQuads(K key, List<MutableQuad> quads) {
            cache.appendAsMutable(mapper.getInternKey(key), quads);
        }
    }

    public interface IModelKeyMapper<F, T> {
        T getInternKey(F key);
    }
}
