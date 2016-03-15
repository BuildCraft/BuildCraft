package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCacheJoiner;
import buildcraft.core.lib.client.model.ModelCacheJoiner.ModelKeyWrapper;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;
import buildcraft.transport.client.model.PipeModelCacheWire.PipeWireKey;

public class PipeModelCacheAll {
    private static final IModelCache<PipeAllCutoutKey> cacheCutout;
    private static final IModelCache<PipeAllTranslucentKey> cacheTranslucent;

    static {
        List<ModelKeyWrapper<PipeAllCutoutKey, ?>> cutout = new ArrayList<>();
        cutout.add(new ModelKeyWrapper<>(PipeAllCutoutKey::getBaseCutout, PipeModelCacheBase.cacheCutout));
        // TODO: Pluggables!
        cacheCutout = new ModelCacheJoiner<>("pipe.all.cutout", cutout);

        List<ModelKeyWrapper<PipeAllTranslucentKey, ?>> translucent = new ArrayList<>();
        translucent.add(new ModelKeyWrapper<>(PipeAllTranslucentKey::getBaseTranslucent, PipeModelCacheBase.cacheTranslucent));
        // TODO: Pluggables!
        cacheTranslucent = new ModelCacheJoiner<>("pipe.all.transclucent", translucent);
    }

    public static ImmutableList<BakedQuad> getCutoutModel(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
        PipeAllCutoutKey key = new PipeAllCutoutKey(pipe, render, pluggable);
        return cacheCutout.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static ImmutableList<BakedQuad> getTranslucentModel(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
        PipeAllTranslucentKey key = new PipeAllTranslucentKey(pipe, render, pluggable);
        return cacheTranslucent.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static class PipeAllCutoutKey {
        private final PipeBaseCutoutKey cutout;
        private final PipeWireKey wire;
        // TODO: Pluggable key!

        public PipeAllCutoutKey(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
            cutout = new PipeBaseCutoutKey(pipe, render);
            wire = new PipeWireKey(render);
            // TODO: Pluggable key!
        }

        public static PipeBaseCutoutKey getBaseCutout(PipeAllCutoutKey key) {
            return key.cutout;
        }

        public static PipeWireKey getWire(PipeAllCutoutKey key) {
            return key.wire;
        }
    }

    public static class PipeAllTranslucentKey {
        private final PipeBaseTransclucentKey translucent;
        // TODO: Pluggable key!

        public PipeAllTranslucentKey(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
            translucent = new PipeBaseTransclucentKey(render);
            // TODO: Pluggable key!
        }

        public static PipeBaseTransclucentKey getBaseTranslucent(PipeAllTranslucentKey key) {
            return key.translucent;
        }
    }
}
