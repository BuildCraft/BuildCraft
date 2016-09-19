package buildcraft.lib.client.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

import buildcraft.api.core.BCLog;
import buildcraft.lib.config.DetailedConfigOption;

/** Implements a caching system for models with potentially infinite variants. Automatically expires entries after a
 * configurable time period, and up to a maximum number. */
public class ModelCache<K> implements IModelCache<K> {
    private static final DetailedConfigOption OPTION_DEBUG = new DetailedConfigOption("render.cache.debug", "false");

    private final String name;
    private final IModelGenerator<K> generator;
    private final LoadingCache<K, ModelValue> modelCache;

    public ModelCache(String detailedName, IModelGenerator<K> generator) {
        this.generator = generator;
        this.name = detailedName;
        modelCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(CacheLoader.from(this::load));
    }

    private ModelValue load(K key) {
        if (OPTION_DEBUG.getAsBoolean()) {
            BCLog.logger.info("Cache[" + name + "]Miss: " + key);
        }
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

    public interface IModelGenerator<T> {
        List<MutableQuad> generate(T key);
    }

    private class ModelValue {
        private final List<MutableQuad> mutableQuads;
        // Identity because VertexFormat is mutable, so we cannot guarentee that nothing changes it.
        private Map<VertexFormat, ImmutableList<BakedQuad>> bakedQuads = new IdentityHashMap<>();

        public ModelValue(List<MutableQuad> quads) {
            mutableQuads = quads;
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
