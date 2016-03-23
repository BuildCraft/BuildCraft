package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;

import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;

public class MiniChunkSpaceAccessor extends AbstractSpaceAccessor<MiniChunkNode> {
    public final World world;

    public MiniChunkSpaceAccessor(World world) {
        this.world = world;
    }

    @Override
    public MiniChunkPoint loadPoint(MiniChunkNode key) {
        return new MiniChunkPoint(key);
    }

    @Override
    public double heuristicCostBetween(IVirtualPoint<MiniChunkNode> a, IVirtualPoint<MiniChunkNode> b) {
        return a.getPoint().getParent().min.distanceSq(b.getPoint().getParent().min);
    }

    @Override
    public double exactCostBetween(IVirtualPoint<MiniChunkNode> a, IVirtualPoint<MiniChunkNode> b) {
        return 16;// FIXME TEMP
    }

    public class MiniChunkPoint implements IVirtualPoint<MiniChunkNode> {
        final MiniChunkNode node;
        private Set<IVirtualPoint<MiniChunkNode>> connected = null;

        public MiniChunkPoint(MiniChunkNode node) {
            this.node = node;
        }

        @Override
        public Set<IVirtualPoint<MiniChunkNode>> getConnected() {
            if (connected == null) {
                connected = new HashSet<>();
                for (MiniChunkNode other : node.connected) {
                    connected.add(MiniChunkSpaceAccessor.this.getPoint(other));
                }
            }
            return connected;
        }

        @Override
        public IVirtualSpaceAccessor<MiniChunkNode> getSpace() {
            return MiniChunkSpaceAccessor.this;
        }

        @Override
        public MiniChunkNode getPoint() {
            return node;
        }
    }
}
