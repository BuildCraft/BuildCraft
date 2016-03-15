package buildcraft.transport.render.tile;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.client.model.IModelCache;
import buildcraft.core.lib.client.model.ModelCacheHelper;
import buildcraft.core.lib.client.model.ModelCacheMultipleSame;
import buildcraft.core.lib.client.model.MutableQuad;
import buildcraft.transport.PipeRenderState;

public class PipeModelCacheWire {
    static final IModelCache<PipeWireKey> cacheAll;
    private static final IModelCache<PipeWireKeySingle> cacheSingle;

    static {
        cacheSingle = new ModelCacheHelper<>("pipe.wire.single", PipeModelCacheWire::generate);
        cacheAll = new ModelCacheMultipleSame<>("pipe.wire.all", PipeWireKey::getKeys, cacheSingle);
    }

    public static List<BakedQuad> getModel(PipeRenderState state) {
        // PipeCutoutKey key = new PipeCutoutKey(render, sprites);
        // return cacheCutout.bake(key, DefaultVertexFormats.BLOCK);
        return Collections.emptyList();
    }

    private static List<MutableQuad> generate(PipeWireKeySingle key) {
        return Collections.emptyList();
    }

    public static final class PipeWireKey {
        public final ImmutableSet<PipeWireKeySingle> keys;

        public PipeWireKey(PipeRenderState state) {
            ImmutableSet.Builder<PipeWireKeySingle> set = ImmutableSet.builder();
            for (PipeWire wire : PipeWire.VALUES) {
                if (!state.wireMatrix.hasWire(wire)) continue;
                EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);
                for (EnumFacing face : EnumFacing.values()) {
                    if (state.wireMatrix.isWireConnected(wire, face)) connections.add(face);
                }
                set.add(new PipeWireKeySingle(wire, state.wireMatrix.isWireLit(wire), connections));
            }
            keys = set.build();
        }

        public static ImmutableSet<PipeWireKeySingle> getKeys(PipeWireKey key) {
            return key.keys;
        }
    }

    public static final class PipeWireKeySingle {
        public final PipeWire type;
        public final boolean on;
        public final EnumSet<EnumFacing> connections;
        private final int hash;

        public PipeWireKeySingle(PipeWire type, boolean on, EnumSet<EnumFacing> connections) {
            this.type = type;
            this.on = on;
            this.connections = connections;
            hash = Objects.hash(type, on, connections);
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
            PipeWireKeySingle other = (PipeWireKeySingle) obj;
            if (on != other.on) return false;
            if (type != other.type) return false;
            return connections.containsAll(other.connections) && other.connections.containsAll(connections);
        }
    }
}
