package buildcraft.core.lib.client.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.config.DetailedConfigOption;

/** Implements a caching system for models with potentially infinite variants. Automatically expires entries after a
 * configurable time period, and up to a maximum number. */
public class ModelCacheHelper<K> implements IModelCache<K> {
    private final DetailedConfigOption optionCacheSize;
    private final IModelGenerator<K> generator;
    private final LoadingCache<K, ModelValue> modelCache;

    public ModelCacheHelper(String detailedName, IModelGenerator<K> generator) {
        this(detailedName, 160, generator);
    }

    public ModelCacheHelper(String detailedName, int defaultMaxSize, IModelGenerator<K> generator) {
        this.generator = generator;
        optionCacheSize = new DetailedConfigOption("render.cache." + detailedName + ".maxsize", Integer.toString(defaultMaxSize));
        int maxSize = optionCacheSize.getAsInt();
        if (maxSize < 0) maxSize = 0;
        BCLog.logger.info("Making cache " + detailedName + " with a maximum size of " + maxSize);
        modelCache = CacheBuilder.newBuilder().maximumSize(maxSize).build(CacheLoader.from(this::load));
    }

    private ModelValue load(K key) {
        return new ModelValue(generator.generate(key));
    }

    @Override
    public void appendAsMutable(K key, List<MutableQuad> quads) {
        quads.addAll(modelCache.getUnchecked(key).mutableQuads);
    }

    @Override
    public ImmutableList<BakedQuad> bake(K key, VertexFormat format) {
        ModelValue value = modelCache.getUnchecked(key);
        return value.bake(format);
    }

    @Override
    public void render(K key, WorldRenderer wr) {
        for (MutableQuad q : modelCache.getUnchecked(key).mutableQuads) {
            q.render(wr);
        }
    }

    public interface IModelGenerator<T> {
        List<MutableQuad> generate(T key);
    }

    private static class ModelValue {
        private final ImmutableList<MutableQuad> mutableQuads;
        // Identity because VertexFormat is mutable, so we cannot guarentee that nothing changes it.
        private Map<VertexFormat, ImmutableList<BakedQuad>> bakedQuads = new IdentityHashMap<>();

        public ModelValue(List<MutableQuad> quads) {
            mutableQuads = ImmutableList.copyOf(quads);
        }

        public ImmutableList<BakedQuad> bake(VertexFormat format) {
            if (!bakedQuads.containsKey(format)) {
                ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
                for (MutableQuad mutable : mutableQuads) {
                    builder.add(mutable.toUnpacked(format));
                }
                bakedQuads.put(format, builder.build());
            }
            return bakedQuads.get(format);
        }
    }
}
