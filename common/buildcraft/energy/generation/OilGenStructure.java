package buildcraft.energy.generation;

import java.util.function.Predicate;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.energy.BCEnergyFluids;

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
    }

    public static class Spout extends OilGenStructure {
        // FIXME (AlexIIL): This won't support cubic chunks - we'll have to do this differently in compat
        public final BlockPos start;
        public final int radius;
        public final int height;

        public Spout(BlockPos start, ReplaceType replaceType, int radius, int height) {
            super(createBox(start, radius), replaceType);
            this.start = start;
            this.radius = radius;
            this.height = height;
        }

        private static Box createBox(BlockPos start, int radius) {
            BlockPos min = start.add(-radius, 0, -radius);
            // FIXME: This 256 will need to be rethought for cubic chunk support
            BlockPos max = new BlockPos(start.getX() + radius, 256, start.getZ() + radius);
            return new Box(min, max);
        }

        @Override
        protected void generateWithin(World world, Box intersect) {
            int segment = world.getChunkFromBlockCoords(start).getTopFilledSegment();
            BlockPos worldTop = world.getTopSolidOrLiquidBlock(start);
            BCLog.logger.info("worldTop = " + worldTop + ", segment = " + segment);
            BlockPos ourTop = worldTop.add(0, height, 0);
            if (ourTop.getY() >= world.getHeight()) {
                ourTop = VecUtil.replaceValue(ourTop, Axis.Y, world.getHeight());
            }
            OilGenerator.createTubeY(start, height, radius).generate(world, intersect);
        }
    }
}
