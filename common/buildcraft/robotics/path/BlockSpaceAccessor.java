package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;

public class BlockSpaceAccessor extends AbstractSpaceAccessor<BlockPos> {
    private final MiniChunkNode node;

    public BlockSpaceAccessor(MiniChunkNode node) {
        this.node = node;
    }

    @Override
    public double heuristicCostBetween(IVirtualPoint<BlockPos> a, IVirtualPoint<BlockPos> b) {
        return Math.sqrt(a.getPoint().distanceSq(b.getPoint()));
    }

    @Override
    public double exactCostBetween(IVirtualPoint<BlockPos> a, IVirtualPoint<BlockPos> b) {
        return node.getExpense(b.getPoint());
    }

    @Override
    public IVirtualPoint<BlockPos> loadPoint(BlockPos key) {
        return new BlockPosPoint(key);
    }

    public class BlockPosPoint implements IVirtualPoint<BlockPos> {
        public final BlockPos point;
        private Set<IVirtualPoint<BlockPos>> connected;

        public BlockPosPoint(BlockPos point) {
            this.point = point;
        }

        @Override
        public Set<IVirtualPoint<BlockPos>> getConnected() {
            if (connected == null) {
                connected = new HashSet<>();
                for (EnumFacing face : EnumFacing.VALUES) {
                    BlockPos offset = point.offset(face);
                    if (!TaskMiniChunkAnalyser.isValid(offset)) continue;
                    int gId = node.getParent().graphArray[offset.getX()][offset.getY()][offset.getZ()];
                    if (gId == node.id) {
                        connected.add(getSpace().getPoint(offset));
                    }
                }
            }
            return connected;
        }

        @Override
        public IVirtualSpaceAccessor<BlockPos> getSpace() {
            return BlockSpaceAccessor.this;
        }

        @Override
        public BlockPos getPoint() {
            return point;
        }
    }
}
