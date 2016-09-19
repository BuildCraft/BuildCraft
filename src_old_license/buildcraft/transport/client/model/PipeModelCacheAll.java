package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;

import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCacheJoiner;
import buildcraft.lib.client.model.ModelCacheJoiner.ModelKeyWrapper;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipePluggableState;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTransclucentKey;
import buildcraft.transport.client.model.PipeModelCachePluggable.PluggableKey;

public class PipeModelCacheAll {
    private static final IModelCache<PipeAllCutoutKey> cacheCutout;
    private static final IModelCache<PipeAllTranslucentKey> cacheTranslucent;

    static {
        List<ModelKeyWrapper<PipeAllCutoutKey, ?>> cutout = new ArrayList<>();
        cutout.add(new ModelKeyWrapper<>(PipeAllCutoutKey::getBaseCutout, PipeModelCacheBase.cacheCutout));
        cutout.add(new ModelKeyWrapper<>(PipeAllCutoutKey::getPluggable, PipeModelCachePluggable.cacheCutoutAll));
        cacheCutout = new ModelCacheJoiner<>("pipe.all.cutout", cutout);

        List<ModelKeyWrapper<PipeAllTranslucentKey, ?>> translucent = new ArrayList<>();
        translucent.add(new ModelKeyWrapper<>(PipeAllTranslucentKey::getBaseTranslucent, PipeModelCacheBase.cacheTranslucent));
        translucent.add(new ModelKeyWrapper<>(PipeAllTranslucentKey::getPluggable, PipeModelCachePluggable.cacheTranslucentAll));
        cacheTranslucent = new ModelCacheJoiner<>("pipe.all.transclucent", translucent);
    }

    public static List<BakedQuad> getCutoutModel(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
        PipeAllCutoutKey key = new PipeAllCutoutKey(pipe, render, pluggable);
        return cacheCutout.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static List<BakedQuad> getTranslucentModel(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
        PipeAllTranslucentKey key = new PipeAllTranslucentKey(pipe, render, pluggable);
        return cacheTranslucent.bake(key, DefaultVertexFormats.BLOCK);
    }

    public static class PipeAllCutoutKey {
        private final PipeBaseCutoutKey cutout;
        private final PluggableKey pluggable;
        private final int hash;

        public PipeAllCutoutKey(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
            cutout = new PipeBaseCutoutKey(pipe, render);
            this.pluggable = new PluggableKey(BlockRenderLayer.CUTOUT, pluggable);
            hash = Objects.hash(cutout, pluggable);
        }

        public PipeBaseCutoutKey getBaseCutout() {
            return cutout;
        }

        public PluggableKey getPluggable() {
            return pluggable;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PipeAllCutoutKey other = (PipeAllCutoutKey) obj;
            if (!cutout.equals(other.cutout)) return false;
            if (!pluggable.equals(other.pluggable)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "PipeAllCutoutKey [base=" + cutout + ", pluggable = " + pluggable + "]";
        }
    }

    public static class PipeAllTranslucentKey {
        private final PipeBaseTransclucentKey translucent;
        private final PluggableKey pluggable;
        private final int hash;

        public PipeAllTranslucentKey(Pipe<?> pipe, PipeRenderState render, PipePluggableState pluggable) {
            translucent = new PipeBaseTransclucentKey(render);
            this.pluggable = new PluggableKey(BlockRenderLayer.TRANSLUCENT, pluggable);
            hash = Objects.hash(translucent, pluggable);
        }

        public PipeBaseTransclucentKey getBaseTranslucent() {
            return translucent;
        }

        public PluggableKey getPluggable() {
            return pluggable;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PipeAllTranslucentKey other = (PipeAllTranslucentKey) obj;
            if (!translucent.equals(other.translucent)) return false;
            if (!pluggable.equals(other.pluggable)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "PipeAllTranslucentKey [base=" + translucent + ", pluggable = " + pluggable + "]";
        }
    }
}
