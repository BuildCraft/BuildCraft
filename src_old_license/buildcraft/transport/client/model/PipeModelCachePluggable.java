package buildcraft.transport.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCache;
import buildcraft.core.lib.client.model.ModelCacheMultipleSame;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.transport.PipePluggableState;

public class PipeModelCachePluggable {
    public static final IModelCache<PluggableKey> cacheCutoutAll, cacheTranslucentAll;
    public static final ModelCache<PluggableModelKey<?>> cacheCutoutSingle, cacheTranslucentSingle;

    static {
        cacheCutoutSingle = new ModelCache<>("pipe.pluggable.cutout.single", PipeModelCachePluggable::generate);
        cacheCutoutAll = new ModelCacheMultipleSame<>("pipe.pluggable.cutout.all", PluggableKey::getKeys, cacheCutoutSingle);

        cacheTranslucentSingle = new ModelCache<>("pipe.pluggable.translucent.single", PipeModelCachePluggable::generate);
        cacheTranslucentAll = new ModelCacheMultipleSame<>("pipe.pluggable.translucent.all", PluggableKey::getKeys, cacheTranslucentSingle);
    }

    private static <K extends PluggableModelKey<K>> ImmutableList<MutableQuad> generate(PluggableModelKey<K> key) {
        if (key == null) return ImmutableList.of();
        ImmutableList.Builder<MutableQuad> builder = ImmutableList.builder();
        VertexFormat format = key.baker.getVertexFormat();
        for (BakedQuad bq : key.baker.bake((K) key)) {
            builder.add(MutableQuad.create(bq, format));
        }
        return builder.build();
    }

    public static class PluggableKey {
        private final ImmutableSet<PluggableModelKey<?>> pluggables;
        private final int hash;

        public PluggableKey(BlockRenderLayer layer, PipePluggableState state) {
            ImmutableSet.Builder<PluggableModelKey<?>> builder = ImmutableSet.builder();
            for (EnumFacing side : EnumFacing.values()) {
                PipePluggable pluggable = state.getPluggable(side);
                if (pluggable == null) continue;
                PluggableModelKey<?> key = pluggable.getModelRenderKey(layer, side);
                if (key == null || key.baker == null) continue;
                builder.add(key);
            }
            this.pluggables = builder.build();
            this.hash = pluggables.hashCode();
        }

        public ImmutableSet<PluggableModelKey<?>> getKeys() {
            return pluggables;
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
            PluggableKey other = (PluggableKey) obj;
            if (!pluggables.equals(other.pluggables)) return false;
            return true;
        }
    }
}
