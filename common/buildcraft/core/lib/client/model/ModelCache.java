package buildcraft.core.lib.client.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.config.DetailedConfigOption;

/** Implements a caching system for models with potentially infinite variants. Automatically expires entries after a
 * configurable time period, and up to a maximum number. */
public class ModelCache<K> implements IModelCache<K> {
    private static final DetailedConfigOption OPTION_DEBUG = new DetailedConfigOption("render.cache.debug", "false");

    private final String name;
    private final DetailedConfigOption optionCacheSize;
    private final IModelGenerator<K> generator;
    private final LoadingCache<K, ModelValue> modelCache;
    private final boolean keepMutable, needGL;
    private final VertexFormat glVertexFormat;

    public ModelCache(String detailedName, IModelGenerator<K> generator) {
        this(detailedName, 1600, generator);
    }

    public ModelCache(String detailedName, int defaultMaxSize, IModelGenerator<K> generator) {
        this(new ModelCacheBuilder<>(detailedName, generator).setMaxSize(defaultMaxSize));
    }

    public ModelCache(ModelCacheBuilder<K> builder) {
        this.generator = builder.generator;
        this.name = builder.detailedName;
        int defaultMaxSize = builder.maxSize;
        optionCacheSize = new DetailedConfigOption("render.cache." + name + ".maxsize", Integer.toString(defaultMaxSize));
        int maxSize = optionCacheSize.getAsInt();
        if (maxSize < 0) maxSize = 0;
        if (OPTION_DEBUG.getAsBoolean()) {
            BCLog.logger.info("Making cache " + name + " with a maximum size of " + maxSize);
        }
        modelCache = CacheBuilder.newBuilder().maximumSize(maxSize).removalListener(this::onRemove).build(CacheLoader.from(this::load));
        keepMutable = builder.keepMutable;
        needGL = builder.needGL;
        this.glVertexFormat = builder.glVertexFormat;
    }

    private void onRemove(RemovalNotification<K, ModelValue> notification) {
        if (OPTION_DEBUG.getAsBoolean()) {
            BCLog.logger.info("Cache[" + name + "]Remove: " + notification.getKey());
        }
        notification.getValue().cleanup();
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

    @Override
    public void render(K key, WorldRenderer wr) {
        for (MutableQuad q : modelCache.getUnchecked(key).mutableQuads) {
            q.render(wr);
        }
    }

    @Override
    public void renderDisplayList(K key) {
        ModelValue value = modelCache.getUnchecked(key);
        GL11.glCallList(value.glDisplayList);
    }

    public interface IModelGenerator<T> {
        List<MutableQuad> generate(T key);
    }

    private class ModelValue {
        private final ImmutableList<MutableQuad> mutableQuads;
        // Identity because VertexFormat is mutable, so we cannot guarentee that nothing changes it.
        private Map<VertexFormat, ImmutableList<BakedQuad>> bakedQuads = new IdentityHashMap<>();
        private int glDisplayList;

        public ModelValue(List<MutableQuad> quads) {
            if (keepMutable) mutableQuads = ImmutableList.copyOf(quads);
            else mutableQuads = ImmutableList.of();
            if (needGL) {
                glDisplayList = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(glDisplayList, GL11.GL_COMPILE);

                Tessellator t = Tessellator.getInstance();
                WorldRenderer wr = t.getWorldRenderer();
                wr.begin(GL11.GL_QUADS, glVertexFormat);
                for (MutableQuad q : quads) {
                    q.render(wr);
                }
                t.draw();

                GL11.glEndList();
            } else {
                glDisplayList = -1;
            }
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

        private void cleanup() {
            if (glDisplayList > 0) {
                GLAllocation.deleteDisplayLists(glDisplayList);
                glDisplayList = -1;
            }
        }
    }
}
