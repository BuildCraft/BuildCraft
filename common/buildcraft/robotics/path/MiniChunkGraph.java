package buildcraft.robotics.path;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class MiniChunkGraph {
    public enum ChunkType {
        COMPLETLY_FREE,
        SINGLE_GRAPH,
        MULTIPLE_GRAPHS,
        COMPLETLY_FILLED
    }

    public final ChunkType type;
    public final Map<EnumFacing, MiniChunkGraph> neighbours = new EnumMap<>(EnumFacing.class);
    public final ImmutableList<NodeBase> nodes;

    public MiniChunkGraph(ChunkType type, ImmutableList<NodeBase> nodes) {
        this.type = type;
        this.nodes = nodes;
    }

    public static abstract class NodeBase {
        public final BlockPos min;
        final Set<NodeBase> connected = Sets.newIdentityHashSet();

        public NodeBase(BlockPos min) {
            this.min = min;
        }

        public Set<NodeBase> getConnected() {
            return Collections.unmodifiableSet(connected);
        }

        /** Checks if this node contains the given position. This will be the world position of the block */
        public abstract boolean contains(BlockPos pos);
    }

    public static class AirNode extends NodeBase {
        public AirNode(BlockPos min) {
            super(min);
        }

        @Override
        public boolean contains(BlockPos pos) {
            return MiniChunkAnalyser.isValid(pos.subtract(min));
        }
    }

    public static class MultiNode extends NodeBase {
        /* FIXME: Do this better! This will currently make a BlockPos instance for every air block in every not-quite
         * empty chunk! */
        private final ImmutableSet<BlockPos> contained;

        public MultiNode(BlockPos min, Set<BlockPos> contained) {
            super(min);
            this.contained = ImmutableSet.copyOf(contained);
        }

        @Override
        public boolean contains(BlockPos pos) {
            return contained.contains(pos);
        }
    }
}
