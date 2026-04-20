package ct.buildcraft.energy.generation.features;

import java.util.function.Predicate;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.enums.EnumSpring;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.block.BlockSpring;
import ct.buildcraft.energy.BCEnergyFluids;
import ct.buildcraft.energy.tile.TileSpringOil;
import ct.buildcraft.lib.BCLib;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class OilStructure {
    public final Box box;
    public final ReplaceType replaceType;
    protected final static FluidState crudeOil = BCEnergyFluids.crudeOil[0].defaultFluidState();

    public OilStructure(Box containingBox, ReplaceType replaceType) {
        this.box = containingBox;
        this.replaceType = replaceType;
    }
    
    public final void generate(WorldGenLevel world, Box within) {
        Box intersect = box.getIntersect(within);
        if (intersect != null) {
            generateWithin(world, intersect);
        }
    }

    /** Generates this structure in the world, but only between the given coordinates. */
    protected abstract void generateWithin(WorldGenLevel world, Box intersect);


    protected abstract int countOilBlocks();

    public void setOilIfCanReplace(WorldGenLevel world, BlockPos pos) {
        if (canReplaceForOil(world, pos)) {
            setOil(world, pos);
        }
    }

    public boolean canReplaceForOil(WorldGenLevel world, BlockPos pos) {
        return replaceType.canReplace(world, pos);
    }

    public static void setOil(WorldGenLevel world, BlockPos pos) {
        world.setBlock(pos, crudeOil.createLegacyBlock(), 2);
        world.scheduleTick(pos, crudeOil.getType(), 0);
    }

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                BlockState state = world.getBlockState(pos);
                if (state.is(BlockTags.LOGS)) {
                    return false;
                }
                ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock());
                if (key != null && key.getPath().contains("mushroom")) {
                    return false;
                }
                return true;
            }
        },
        IS_FOR_LAKE {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                return ALWAYS.canReplace(world, pos);//TODO
            }
        };
        public abstract boolean canReplace(WorldGenLevel world, BlockPos pos);
    }

    public static class GenByPredicate extends OilStructure {
        public final Predicate<BlockPos> predicate;

        public GenByPredicate(Box containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate) {
            super(containingBox, replaceType);
            this.predicate = predicate;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
                if (predicate.test(pos)) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        protected int countOilBlocks() {
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.min(), box.max())) {
                if (predicate.test(pos)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class FlatPattern extends OilStructure {
        private final boolean[][] pattern;
        private final int depth;

        private FlatPattern(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
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
        protected void generateWithin(WorldGenLevel world, Box intersect) {
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

    public static class PatternTerrainHeight extends OilStructure {
        private final boolean[][] pattern;
        private final int depth;

        private PatternTerrainHeight(Box containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static PatternTerrainHeight create(BlockPos.MutableBlockPos start, ReplaceType replaceType, boolean[][] pattern,
            int depth) {
            BlockPos min = start.setY(1).immutable();
            BlockPos max = start.move(pattern.length - 1, 255, pattern.length == 0 ? 0 : pattern[0].length - 1).immutable();
            Box box = new Box(min, max);
            return new PatternTerrainHeight(box, replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
        	MutableBlockPos pos = new MutableBlockPos();
            for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
                int px = x - box.min().getX();

                for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
                    int pz = z - box.min().getZ();

                    if (pattern[px][pz]) {
                        BlockPos.MutableBlockPos upper = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos.set(x, 0, z)).mutable().move(0, -1, 0);//TODO CHECK
                        int h = upper.getY();
                        if (canReplaceForOil(world, upper)) {
                            for (int y = 0; y < 5; y++) {
                                world.setBlock(upper.setY(y+h), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                            }
                            for (int y = 0; y < depth; y++) {
                                setOilIfCanReplace(world, upper.setY(h-y));
                            }
                        }
                    }
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

    public static class Spout extends OilStructure {
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
            // Only a block 1 x level.getHeight()(default 384) x 1 -- that way we area only called once.
            return new Box(start, VecUtil.replaceValue(start, Axis.Y, OilGenerator.worldHeight));
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            count = 0;
            int segment = world.getChunk(start).getHighestSectionPosition();
            BlockPos worldTop = new BlockPos(start.getX(), segment + 16, start.getZ());
            for (int y = segment; y >= start.getY(); y--) {
                worldTop = worldTop.below();
                BlockState state = world.getBlockState(worldTop);
                if (state.isAir()) {
                    continue;
                }
                if (BlockUtil.getFluidWithoutFlowing(state) != Fluids.EMPTY) {//TODO CHECK!
                    break;
                }
                if (state.getMaterial().blocksMotion()) {
                    break;
                }
            }
            OilStructure tubeY = OilGenerator.createTube(start, worldTop.getY() - start.getY(), radius, Axis.Y);
            tubeY.generate(world, tubeY.box);
            count += tubeY.countOilBlocks();
            BlockPos base = worldTop;
            for (int r = radius; r >= 0; r--) {
                // BCLog.logger.info(" - " + base + " = " + r);
                OilStructure struct = OilGenerator.createTube(base, height, r, Axis.Y);
                struct.generate(world, struct.box);
                base = base.offset(0, height, 0);
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

    public static class Spring extends OilStructure {
        public final BlockPos pos;

        public Spring(BlockPos pos) {
            super(new Box(pos, pos), ReplaceType.ALWAYS);
            this.pos = pos;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, Box intersect) {
            // NO-OP (this one is called separately)
        }

        @Override
        protected int countOilBlocks() {
            return 0;
        }

        public void generate(WorldGenLevel world, int count) {
            BlockState state = BCCoreBlocks.SPRING.get().defaultBlockState();
            state = state.setValue(BlockSpring.SPRING_TYPE, EnumSpring.OIL);
            //BCLog.logger.debug("OilGenStruecutre:1 generate spring for "+pos);
            world.setBlock(pos, state, 2);
            BlockEntity tile = world.getBlockEntity(pos);
            TileSpringOil spring;
            if (tile instanceof TileSpringOil) {
                spring = (TileSpringOil) tile;
                spring.totalSources = count;
            } else {
                BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + pos);
                spring = new TileSpringOil(pos, state);
                ServerLevel level = world.getLevel();
                spring.setLevel(level);
                level.setBlockEntity(spring);
            }
            spring.totalSources = count;
            if (BCLib.DEV) {
                BCLog.logger.info("[energy.gen.oil] Generated TileSpringOil as " + System.identityHashCode(tile));
            }
        }
    }
}
