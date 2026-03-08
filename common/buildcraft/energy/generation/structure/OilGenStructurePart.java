package buildcraft.energy.generation.structure;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.lib.BCLib;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.LiquidsConfig;
import net.minecraft.world.gen.feature.SpringFeature;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;
import java.util.function.Predicate;

public abstract class OilGenStructurePart {
    public final Box box;
    public final ReplaceType replaceType;

    public OilGenStructurePart(Box containingBox, ReplaceType replaceType) {
        this.box = containingBox;
        this.replaceType = replaceType;
    }

    public final void generate(IWorld world, Box within) {
        Box intersect = box.getIntersect(within);
        if (intersect != null) {
            generateWithin(world, intersect);
        }
    }

    /** Generates this structure in the world, but only between the given coordinates. */
    protected abstract void generateWithin(IWorld world, Box intersect);

    /** @return The number of oil blocks that this structure will set. Note that this is called *after*
     *         {@link #generateWithin(IWorld, Box)}, by the Spring type, so this can store the number set. */
    public abstract int countOilBlocks();

    public void setOilIfCanReplace(IWorld world, BlockPos pos) {
        if (canReplaceForOil(world, pos)) {
            setOil(world, pos);
        }
    }

    public boolean canReplaceForOil(IWorld world, BlockPos pos) {
        return replaceType.canReplace(world, pos);
    }

    private static final BlockState OIL_BLOCK_STATE = BCEnergyFluids.crudeOil[0].get().getSource().defaultFluidState().createLegacyBlock();
    private static final Fluid OIL_FLUID = BCEnergyFluids.crudeOil[0].get().getSource().defaultFluidState().getType();

    /**
     * Set the block to cruel oil.
     * We should call {@link ITickList#scheduleTick(BlockPos, Object, int)} to make oil flow in {@link WorldGenRegion}.
     * Just like {@link SpringFeature#place(ISeedReader, ChunkGenerator, Random, BlockPos, LiquidsConfig)}
     *
     * @param world This will be {@link ServerWorld} when a player makes an oil well, while {@link WorldGenRegion} when an oil well generates naturally
     * @param pos The block pos we set to oil block
     */
    public static void setOil(IWorld world, BlockPos pos) {
        world.setBlock(pos, OIL_BLOCK_STATE, BlockConstants.UPDATE_ALL);
        world.getLiquidTicks().scheduleTick(pos, OIL_FLUID, 0);
    }

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(IWorld world, BlockPos pos) {
                return true;
            }
        },
        IS_FOR_LAKE {
            @Override
            public boolean canReplace(IWorld world, BlockPos pos) {
                return ALWAYS.canReplace(world, pos);
            }
        };

        public abstract boolean canReplace(IWorld world, BlockPos pos);
    }

    /**
     * In 1.18.2, we should save the structure pieces as NBT data, and the NBT will be deserialized for each chunk around the center for generation
     * So, we should serialize the predicate...
     */
    public static class GenByPredicate extends OilGenStructurePart {
        public final Predicate<BlockPos> predicate;
        // 1.18.2: Only for NBT Serialize
        public final Object[] predicateArgs;

        // public GenByPredicate(Box containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate)
        public GenByPredicate(Box containingBox, ReplaceType replaceType, Object... predicateArgs) {
            super(containingBox, replaceType);
            this.predicateArgs = predicateArgs;
            Predicate<BlockPos> tester;
            if (predicateArgs.length == 2) {
                tester = p -> VecUtil.distanceSq(p, (BlockPos) predicateArgs[0]) <= (double) predicateArgs[1];
            } else if (predicateArgs.length == 4) {
                tester = p -> VecUtil.distanceSq(VecUtil.replaceValue(p, (Axis) predicateArgs[0], (int) predicateArgs[1]), (BlockPos) predicateArgs[2]) <= (double) predicateArgs[3];
            } else {
                throw new RuntimeException("Unexpected Predict Type! Predict Types are Limited for NBT Serialize");
            }
            this.predicate = tester;
        }

        @Override
        protected void generateWithin(IWorld world, Box intersect) {
            for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
                if (predicate.test(pos)) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        public int countOilBlocks() {
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.min(), box.max())) {
                if (predicate.test(pos)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class FlatPattern extends OilGenStructurePart {
        public final boolean[][] pattern;
        public final int depth;

        public FlatPattern(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static FlatPattern create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
            BlockPos min = start.offset(0, 1 - depth, 0);
            BlockPos max = start.offset(pattern.length - 1, 0, pattern.length == 0 ? 0 : pattern[0].length - 1);
            Box box = new Box(min, max);
            return new FlatPattern(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(IWorld world, Box intersect) {
            BlockPos start = box.min();
            for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
                int x = pos.getX() - start.getX();
                int z = pos.getZ() - start.getZ();
                if (pattern[x][z]) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        public int countOilBlocks() {
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

    public static class PatternTerrainHeight extends OilGenStructurePart {
        public final boolean[][] pattern;
        public final int depth;

        public PatternTerrainHeight(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static PatternTerrainHeight create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
            BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
            BlockPos max = min.offset(pattern.length - 1, 255, pattern.length == 0 ? 0 : pattern[0].length - 1);
            Box box = new Box(min, max);
            return new PatternTerrainHeight(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(IWorld world, Box intersect) {
            for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
                int px = x - box.min().getX();

                for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
                    int pz = z - box.min().getZ();

                    if (pattern[px][pz]) {
//                        BlockPos upper = world.getHeight(new BlockPos(x, 0, z)).down();
                        BlockPos upper = world.getHeightmapPos(
                                world instanceof WorldGenRegion ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.WORLD_SURFACE,
                                new BlockPos(x, 0, z)
                        ).below();
                        if (canReplaceForOil(world, upper)) {
                            for (int y = 0; y < 5; y++) {
                                world.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL);
                            }
                            for (int y = 0; y < depth; y++) {
                                setOilIfCanReplace(world, upper.below(y));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int countOilBlocks() {
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

    public static class Spout extends OilGenStructurePart {
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
        protected void generateWithin(IWorld world, Box intersect) {
            count = 0;
            int segment = world.getChunk(start).getHighestSectionPosition();
            BlockPos worldTop = new BlockPos(start.getX(), segment + 16, start.getZ());
            for (int y = segment; y >= start.getY(); y--) {
                worldTop = worldTop.below();
                BlockState state = world.getBlockState(worldTop);
                if (world.isEmptyBlock(worldTop)) {
                    continue;
                }
                if (BlockUtil.getFluidWithFlowing(state.getBlock()) != null) {
                    break;
                }
                if (state.getMaterial().blocksMotion()) {
                    break;
                }
            }
            OilGenStructurePart tubeY = OilGenerator.createTube(start, worldTop.getY() - start.getY(), radius, Axis.Y);
            tubeY.generate(world, tubeY.box);
            count += tubeY.countOilBlocks();
            BlockPos base = worldTop;
            for (int r = radius; r >= 0; r--) {
                // BCLog.logger.info(" - " + base + " = " + r);
                OilGenStructurePart struct = OilGenerator.createTube(base, height, r, Axis.Y);
                struct.generate(world, struct.box);
                base = base.offset(0, height, 0);
                count += struct.countOilBlocks();
            }
        }

        @Override
        public int countOilBlocks() {
            if (count == 0) {
                throw new IllegalStateException("Called countOilBlocks before calling generateWithin!");
            }
            return count;
        }
    }

    public static class Spring extends OilGenStructurePart {
        public final BlockPos pos;

        public Spring(BlockPos pos) {
            super(new Box(pos, pos), ReplaceType.ALWAYS);
            this.pos = pos;
        }

        @Override
        protected void generateWithin(IWorld world, Box intersect) {
            // NO-OP (this one is called separately)
        }

        @Override
        public int countOilBlocks() {
            return 0;
        }

        public void generate(IWorld world, int count) {
            BlockState state = BCCoreBlocks.springOil.get().defaultBlockState();
//            state = state.setValue(BuildCraftProperties.SPRING_TYPE, EnumSpring.OIL);
            world.setBlock(pos, state, BlockConstants.UPDATE_ALL);
            TileEntity tile = world.getBlockEntity(pos);
            TileSpringOil spring;
            if (tile instanceof TileSpringOil) {
                spring = (TileSpringOil) tile;
            } else {
                BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at [" + pos + "], something goes wrong...");
                spring = new TileSpringOil();
//                world.setBlockEntity(pos, spring);
//                spring.setLevelAndPosition(world, count);
            }
            spring.totalSources = count;
            if (BCLib.DEV) {
                BCLog.logger.info("[energy.gen.oil] Generated TileSpringOil as " + System.identityHashCode(tile));
            }
        }
    }
}
