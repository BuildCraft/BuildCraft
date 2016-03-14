package buildcraft.transport.render.tile;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCacheJoiner;
import buildcraft.core.lib.client.model.ModelCacheJoiner.ModelKeyWrapper;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.render.tile.PipeModelCacheBase.PipeCutoutKey;
import buildcraft.transport.render.tile.PipeModelCacheBase.PipeTransclucentKey;
import buildcraft.transport.render.tile.PipeModelCacheWire.PipeWireKey;

public class PipeModelCacheAll {
    private static final IModelCache<PipeKey> cacheCutout;
    private static final IModelCache<PipeKey> cacheTranslucent;

    static {
        List<ModelKeyWrapper<PipeKey, ?>> cutout = new ArrayList<>();
        cutout.add(new ModelKeyWrapper<>(PipeKey::getBaseCutout, PipeModelCacheBase.cacheCutout));
        cutout.add(new ModelKeyWrapper<>(PipeKey::getWire, PipeModelCacheWire.cacheAll));
        cacheCutout = new ModelCacheJoiner<>("pipe.all.cutout", cutout);

        List<ModelKeyWrapper<PipeKey, ?>> translucent = new ArrayList<>();
        translucent.add(new ModelKeyWrapper<>(PipeKey::getBaseTranslucent, PipeModelCacheBase.cacheTranslucent));
        cacheTranslucent = new ModelCacheJoiner<>("pipe.all.transclucent", translucent);
    }

    public static ImmutableList<BakedQuad> getCutoutModel(PipeRenderState render, PipePluggableState pluggable) {
        PipeKey key = new PipeKey(render, pluggable);
        return cacheCutout.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static ImmutableList<BakedQuad> getTranslucentModel(PipeRenderState render, PipePluggableState pluggable) {
        PipeKey key = new PipeKey(render, pluggable);
        return cacheTranslucent.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static class PipeKey {
        private final PipeCutoutKey cutout;
        private final PipeTransclucentKey transclucent;
        private final PipeWireKey wire;

        public PipeKey(PipeRenderState render, PipePluggableState pluggable) {
            cutout = new PipeCutoutKey(render);
            transclucent = new PipeTransclucentKey(render);
            wire = new PipeWireKey(render);
        }

        public static PipeCutoutKey getBaseCutout(PipeKey key) {
            return key.cutout;
        }

        public static PipeTransclucentKey getBaseTranslucent(PipeKey key) {
            return key.transclucent;
        }

        public static PipeWireKey getWire(PipeKey key) {
            return key.wire;
        }
    }
}
