package buildcraft.transport.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKeyCutout;
import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCache;
import buildcraft.core.lib.client.model.ModelCacheMultipleSame;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.transport.PipePluggableState;

public class PipeModelCachePluggableCutout {
    public static final IModelCache<PluggableCutoutKey> cacheAll;
    public static final ModelCache<PluggableModelKeyCutout<?>> cacheSingle;

    static {
        cacheSingle = new ModelCache<>("pipe.pluggable.single", PipeModelCachePluggableCutout::generate);
        cacheAll = new ModelCacheMultipleSame<>("pipe.pluggable.all", PluggableCutoutKey::getKeys, cacheSingle);
    }

    public static <K extends PluggableModelKeyCutout<K>> ImmutableList<MutableQuad> generate(PluggableModelKeyCutout<K> key) {
        if (key == null) return ImmutableList.of();
        ImmutableList.Builder<MutableQuad> builder = ImmutableList.builder();
        VertexFormat format = key.baker.getVertexFormat();
        for (BakedQuad bq : key.baker.bakeCutout((K) key)) {
            builder.add(MutableQuad.create(bq, format));
        }
        return builder.build();
    }

    public static class PluggableCutoutKey {
        private final ImmutableSet<PluggableModelKeyCutout<?>> pluggables;
        private final int hash;

        public PluggableCutoutKey(ImmutableSet<PluggableModelKeyCutout<?>> pluggables) {
            this.pluggables = pluggables;
            this.hash = pluggables.hashCode();
        }

        public PluggableCutoutKey(PipePluggableState state) {
            ImmutableSet.Builder<PluggableModelKeyCutout<?>> builder = ImmutableSet.builder();
            for (EnumFacing side : EnumFacing.values()) {
                PipePluggable pluggable = state.getPluggable(side);
                if (pluggable == null) continue;
                PluggableModelKeyCutout<?> key = pluggable.getModelRenderKey(side);
                if (key == null || key.baker == null) continue;
                builder.add(key);
            }
            this.pluggables = builder.build();
            this.hash = pluggables.hashCode();
        }

        public ImmutableSet<PluggableModelKeyCutout<?>> getKeys() {
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
            PluggableCutoutKey other = (PluggableCutoutKey) obj;
            if (!pluggables.equals(other.pluggables)) return false;
            return true;
        }
    }
}
