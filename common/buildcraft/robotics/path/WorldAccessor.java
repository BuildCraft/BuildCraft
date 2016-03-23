package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldAccessor extends AbstractSpaceAccessor<BlockPos> {
    private final World world;

    public WorldAccessor(World world) {
        this.world = world;
    }

    @Override
    public double heuristicCostBetween(IVirtualPoint<BlockPos> a, IVirtualPoint<BlockPos> b) {
        return Math.sqrt(a.getPoint().distanceSq(b.getPoint()));
    }

    @Override
    public double exactCostBetween(IVirtualPoint<BlockPos> a, IVirtualPoint<BlockPos> b) {
        return exactCostOf(b.getPoint());
    }

    private double exactCostOf(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(world, pos)) return 1;
        else if (block.getMaterial().isLiquid()) return 3;
        else return Integer.MAX_VALUE;
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
                    if (world.isBlockLoaded(offset)) {
                        if (exactCostOf(offset) < 100) {
                            connected.add(getSpace().getPoint(offset));
                        }
                    }
                }
            }
            return connected;
        }

        @Override
        public IVirtualSpaceAccessor<BlockPos> getSpace() {
            return WorldAccessor.this;
        }

        @Override
        public BlockPos getPoint() {
            return point;
        }
    }
}
