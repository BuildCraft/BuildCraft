package buildcraft.energy.generation;

import java.util.function.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.api.enums.EnumSpring;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockSpring;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.tile.TileSpringOil;

public abstract class OilGenStructure {
    public final Box box;
    public final ReplaceType replaceType;

    public OilGenStructure(Box containingBox, ReplaceType replaceType) {
        this.box = containingBox;
        this.replaceType = replaceType;
    }

    public final void generate(World world, Box within) {
        Box intersect = box.getIntersect(within);
        if (intersect != null) {
            generateWithin(world, intersect);
        }
    }

    /** Generates this structure in the world, but only between the given coordinates. */
    protected abstract void generateWithin(World world, Box intersect);

    /** @return The number of oil blocks that this structure will set. Note that this is called *after*
     *         {@link #generateWithin(World, Box)}, by the Spring type, so this can store the number set. */
    protected abstract int countOilBlocks();

    public void setOilIfCanReplace(World world, BlockPos pos) {
        if (canReplaceForOil(world, pos)) {
            setOil(world, pos);
        }
    }

    public boolean canReplaceForOil(World world, BlockPos pos) {
        return replaceType.canReplace(world, pos);
    }

    public static void setOil(World world, BlockPos pos) {
        world.setBlockState(pos, BCEnergyFluids.crudeOil[0].getBlock().getDefaultState(), 2);
    }

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(World world, BlockPos pos) {
                return true;
            }
        },
        IS_FOR_LAKE {
            @Override
            public boolean canReplace(World world, BlockPos pos) {
                return ALWAYS.canReplace(world, pos);
            }
        };
        public abstract boolean canReplace(World world, BlockPos pos);
    }

    public static class GenByPredicate extends OilGenStructure {
        public final Predicate<BlockPos> predicate;

        public GenByPredicate(Box containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate) {
            super(containingBox, replaceType);
            this.predicate = predicate;
        }

        @Override
        protected void generateWithin(World world, Box intersect) {
            for (BlockPos pos : BlockPos.getAllInBox(intersect.min(), intersect.max())) {
                if (predicate.test(pos)) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (BlockPos pos : BlockPos.getAllInBox(box.min(), box.max())) {
                if (predicate.test(pos)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class FlatPattern extends OilGenStructure {
        private final boolean[][] pattern;
        private final int depth;

        private FlatPattern(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static FlatPattern create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
            BlockPos min = start.add(0, 1 - depth, 0);
            BlockPos max = start.add(pattern.length - 1, 0, pattern.length == 0 ? 0 : pattern[0].length - 1);
            Box box = new Box(min, max);
            return new FlatPattern(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(World world, Box intersect) {
            BlockPos start = box.min();
            for (BlockPos pos : BlockPos.getAllInBox(intersect.min(), intersect.max())) {
                int x = pos.getX() - start.getX();
                int z = pos.getZ() - start.getZ();
                if (pattern[x][z]) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (int x = 0; x < pattern.length; x++) {
                for (int z = 0; z < pattern[x].length; z++) {
                    if (pattern[x][z]) {
                        count++;
                    }
                }
            }
            return count * depth;
        }
    }

    public static class Spout extends OilGenStructure {
        // FIXME (AlexIIL): This won't support cubic chunks - we'll have to do this differently in compat
        // TODO: Use a terrain generator from mc terrain generation to get the height of the world
        // A hook will go in compat for help when using cubic chunks or a different type of terrain generator
        public final BlockPos start;
        public final int radius;
        public final int height;
        private int count = 0;

        public Spout(BlockPos start, ReplaceType replaceType, int radius, int height) {
            super(createBox(start), replaceType);
            this.start = start;
            this.radius = radius;
            this.height = height;
        }

        private static Box createBox(BlockPos start) {
            // Only a block 1 x 256 x 1 -- that way we area only called once.
            // FIXME: This 256 will need to be rethought for cubic chunk support
            return new Box(start, VecUtil.replaceValue(start, Axis.Y, 256));
        }

        @Override
        protected void generateWithin(World world, Box intersect) {
            count = 0;
            int segment = world.getChunkFromBlockCoords(start).getTopFilledSegment();
            BlockPos worldTop = new BlockPos(start.getX(), segment + 16, start.getZ());
            for (int y = segment; y >= start.getY(); y--) {
                worldTop = worldTop.down();
                IBlockState state = world.getBlockState(worldTop);
                if (state.getBlock().isAir(state, world, worldTop)) {
                    continue;
                }
                if (BlockUtil.getFluidWithFlowing(state.getBlock()) != null) {
                    break;
                }
                if (state.getMaterial().blocksMovement()) {
                    break;
                }
            }
            OilGenStructure tubeY = OilGenerator.createTubeY(start, worldTop.getY() - start.getY(), radius);
            tubeY.generate(world, tubeY.box);
            count += tubeY.countOilBlocks();
            BlockPos base = worldTop;
            for (int r = radius; r >= 0; r--) {
                BCLog.logger.info(" - " + base + " = " + r);
                OilGenStructure struct = OilGenerator.createTubeY(base, height, r);
                struct.generate(world, struct.box);
                base = base.add(0, height, 0);
                count += struct.countOilBlocks();
            }
        }

        @Override
        protected int countOilBlocks() {
            if (count == 0) {
                throw new IllegalStateException("Called countOilBlocks before calling generateWithin!");
            }
            return count;
        }
    }

    public static class Spring extends OilGenStructure {
        public final BlockPos pos;

        public Spring(BlockPos pos) {
            super(new Box(pos, pos), ReplaceType.ALWAYS);
            this.pos = pos;
        }

        @Override
        protected void generateWithin(World world, Box intersect) {
            // NO-OP (this one is called separately)
        }

        @Override
        protected int countOilBlocks() {
            return 0;
        }

        public void generate(World world, int count) {
            IBlockState state = BCCoreBlocks.SPRING.getDefaultState();
            state = state.withProperty(BlockSpring.SPRING_TYPE, EnumSpring.OIL);
            world.setBlockState(pos, state);
            TileEntity tile = world.getTileEntity(pos);
            TileSpringOil spring;
            if (tile instanceof TileSpringOil) {
                spring = (TileSpringOil) tile;
            } else {
                BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + pos);
                spring = new TileSpringOil();
                spring.setWorld(world);
                spring.setPos(pos);
                world.setTileEntity(pos, spring);
            }
            spring.totalSources = count;
            BCLog.logger.info("[energy.gen.oil] Generated TileSpringOil as " + System.identityHashCode(tile)); //  TODO: This might not work properly!
        }
    }
}
