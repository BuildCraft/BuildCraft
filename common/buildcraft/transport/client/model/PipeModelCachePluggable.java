package buildcraft.transport.client.model;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.ModelCacheMultipleSame;
import buildcraft.transport.client.PipeRegistryClient;

public class PipeModelCachePluggable {
    public static final IModelCache<PluggableKey> cacheCutoutAll, cacheTranslucentAll;
    public static final ModelCache<PluggableModelKey> cacheCutoutSingle, cacheTranslucentSingle;

    static {
        cacheCutoutSingle = new ModelCache<>(PipeModelCachePluggable::generate);
        cacheCutoutAll = new ModelCacheMultipleSame<>(PluggableKey::getKeys, cacheCutoutSingle);

        cacheTranslucentSingle = new ModelCache<>(PipeModelCachePluggable::generate);
        cacheTranslucentAll = new ModelCacheMultipleSame<>(PluggableKey::getKeys, cacheTranslucentSingle);
    }

    private static <K extends PluggableModelKey> List<BakedQuad> generate(K key) {
        if (key == null) {
            return ImmutableList.of();
        }
        IPluggableStaticBaker<K> baker = PipeRegistryClient.getPlugBaker(key);
        if (baker == null) {
            return ImmutableList.of();
        }
        return baker.bake(key);
    }

    public static class PluggableKey {
        private final ImmutableSet<PluggableModelKey> pluggables;
        private final int hash;

        public PluggableKey(BlockRenderLayer layer, IPipeHolder holder) {
            ImmutableSet.Builder<PluggableModelKey> builder = ImmutableSet.builder();
            for (EnumFacing side : EnumFacing.VALUES) {
                PipePluggable pluggable = holder.getPluggable(side);
                if (pluggable == null) continue;
                PluggableModelKey key = pluggable.getModelRenderKey(layer);
                if (key == null) continue;
                builder.add(key);
            }
            this.pluggables = builder.build();
            this.hash = pluggables.hashCode();
        }

        public ImmutableSet<PluggableModelKey> getKeys() {
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
