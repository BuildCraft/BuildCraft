package buildcraft.lib.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.Map;

/**
 * The methods are just copied from the super classes and changed some vars
 * to make oils behave like they did in 1.12.2.
 */
public abstract class BCFluid extends ForgeFlowingFluid {
    protected BCFluidRegistryContainer fluidRegistryContainer;
    protected boolean isGas = this.getFluidType().isLighterThanAir();

    protected BCFluid(ForgeFlowingFluid.Properties properties, BCFluidRegistryContainer reg) {
        super(properties);
        this.fluidRegistryContainer = reg;
    }

    public BCFluidRegistryContainer getReg() {
        return fluidRegistryContainer;
    }

    // Flow & Still

    public static class Flowing extends BCFluid {
        public Flowing(Properties properties, BCFluidRegistryContainer reg) {
            super(properties, reg);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends BCFluid {
        public Source(Properties properties, BCFluidRegistryContainer reg) {
            super(properties, reg);
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }

    // ForgeFlowingFluid

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
        // Based on the water implementation, may need to be overriden for mod fluids that shouldn't behave like water.
        return direction == /*here different from ForgeFlowingFluid*/ (isGas ? Direction.UP : Direction.DOWN) && !isSame(fluidIn);
    }

    // FlowingFluid

    /**
     * To make gas spread up.
     *
     * @param world
     * @param pos
     * @param state
     */
    @Override
    protected void spread(Level world, BlockPos pos, FluidState state) {
        Direction spreadDirection = isGas ? Direction.UP : Direction.DOWN;
        if (!state.isEmpty()) {
            BlockState blockstate = world.getBlockState(pos);
            BlockPos blockpos = /*here different from FlowingFluid*/ isGas ? pos.above() : pos.below();
            BlockState blockstate1 = world.getBlockState(blockpos);
            FluidState fluidstate = this.getNewLiquid(world, blockpos, blockstate1);
            if (this.canSpreadTo(world, pos, blockstate, /*here different from FlowingFluid*/ spreadDirection, blockpos, blockstate1, world.getFluidState(blockpos), fluidstate.getType())) {
                this.spreadTo(world, blockpos, blockstate1, /*here different from FlowingFluid*/ spreadDirection, fluidstate);
                if (this.sourceNeighborCount(world, pos) >= 3) {
                    this.spreadToSides(world, pos, state, blockstate);
                }
            } else if (state.isSource() || !this.isWaterHole(world, fluidstate.getType(), pos, blockstate, blockpos, blockstate1)) {
                this.spreadToSides(world, pos, state, blockstate);
            }

        }
    }

    @Override
    protected FluidState getNewLiquid(Level world, BlockPos pos, BlockState state) {
        int i = 0;
        int j = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = world.getBlockState(blockpos);
            FluidState fluidstate = blockstate.getFluidState();
            if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, world, pos, state, blockpos, blockstate)) {
                if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(world, blockpos, blockstate, fluidstate.canConvertToSource(world, blockpos))) {
                    ++j;
                }

                i = Math.max(i, fluidstate.getAmount());
            }
        }

        if (j >= 2) {
            BlockState blockstate1 = world.getBlockState(pos.below());
            FluidState fluidstate1 = blockstate1.getFluidState();
            if (blockstate1.isSolid() || this.isSourceBlockOfThisType(fluidstate1)) {
                return this.getSource(false);
            }
        }

        BlockPos blockpos1 = /*here different from FlowingFluid*/ isGas ? pos.below() : pos.above();
        BlockState blockstate2 = world.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();
        Direction d = isGas ? Direction.DOWN : Direction.UP; // Calen add
        if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(/*here different from FlowingFluid*/ d, world, pos, state, blockpos1, blockstate2)) {
            return this.getFlowing(8, true);
        } else {
            int k = i - this.getDropOff(world);
            return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    @Override
    public Vec3 getFlow(BlockGetter p_75987_, BlockPos p_75988_, FluidState p_75989_) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.setWithOffset(p_75988_, direction);
            FluidState fluidstate = p_75987_.getFluidState(blockpos$mutableblockpos);
            if (this.affectsFlow(fluidstate)) {
                float f = fluidstate.getOwnHeight();
                float f1 = 0.0F;
                if (f == 0.0F) {
                    if (!p_75987_.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
                        BlockPos blockpos = /*here different from FlowingFluid*/ isGas ? blockpos$mutableblockpos.above() : blockpos$mutableblockpos.below();
                        FluidState fluidstate1 = p_75987_.getFluidState(blockpos);
                        if (this.affectsFlow(fluidstate1)) {
                            f = fluidstate1.getOwnHeight();
                            if (f > 0.0F) {
                                f1 = p_75989_.getOwnHeight() - (f - 0.8888889F);
                            }
                        }
                    }
                } else if (f > 0.0F) {
                    f1 = p_75989_.getOwnHeight() - f;
                }

                if (f1 != 0.0F) {
                    d0 += (double) ((float) direction.getStepX() * f1);
                    d1 += (double) ((float) direction.getStepZ() * f1);
                }
            }
        }

        Vec3 vec3 = new Vec3(d0, 0.0D, d1);
        if (p_75989_.getValue(FALLING)) {
            for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                blockpos$mutableblockpos.setWithOffset(p_75988_, direction1);
                if (this.isSolidFace(p_75987_, blockpos$mutableblockpos, direction1) || this.isSolidFace(p_75987_, /*here different from FlowingFluid*/ isGas ? blockpos$mutableblockpos.below() : blockpos$mutableblockpos.above(), direction1)) {
                    vec3 = vec3.normalize().add(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        return vec3.normalize();
    }

    @Override
    protected int getSlopeDistance(LevelReader p_76027_, BlockPos p_76028_, int p_76029_, Direction p_76030_, BlockState p_76031_, BlockPos p_76032_, Short2ObjectMap<Pair<BlockState, FluidState>> p_76033_, Short2BooleanMap p_76034_) {
        int i = 1000;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction != p_76030_) {
                BlockPos blockpos = p_76028_.relative(direction);
                short short1 = getCacheKey(p_76032_, blockpos);
                Pair<BlockState, FluidState> pair = p_76033_.computeIfAbsent(short1, (p_192916_) ->
                {
                    BlockState blockstate1 = p_76027_.getBlockState(blockpos);
                    return Pair.of(blockstate1, blockstate1.getFluidState());
                });
                BlockState blockstate = pair.getFirst();
                FluidState fluidstate = pair.getSecond();
                if (this.canPassThrough(p_76027_, this.getFlowing(), p_76028_, p_76031_, direction, blockpos, blockstate, fluidstate)) {
                    boolean flag = p_76034_.computeIfAbsent(short1, (p_192912_) ->
                    {
                        BlockPos blockpos1 = /*here different from FlowingFluid*/ isGas ? blockpos.above() : blockpos.below();
                        BlockState blockstate1 = p_76027_.getBlockState(blockpos1);
                        return this.isWaterHole(p_76027_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                    });
                    if (flag) {
                        return p_76029_;
                    }

                    if (p_76029_ < this.getSlopeFindDistance(p_76027_)) {
                        int j = this.getSlopeDistance(p_76027_, blockpos, p_76029_ + 1, direction.getOpposite(), blockstate, p_76032_, p_76033_, p_76034_);
                        if (j < i) {
                            i = j;
                        }
                    }
                }
            }
        }

        return i;
    }

    @Override
    protected Map<Direction, FluidState> getSpread(Level p_256191_, BlockPos p_76081_, BlockState p_76082_) {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = p_76081_.relative(direction);
            short short1 = getCacheKey(p_76081_, blockpos);
            Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_192907_) ->
            {
                BlockState blockstate1 = p_256191_.getBlockState(blockpos);
                return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            FluidState fluidstate1 = this.getNewLiquid(p_256191_, blockpos, blockstate);
            if (this.canPassThrough(p_256191_, fluidstate1.getType(), p_76081_, p_76082_, direction, blockpos, blockstate, fluidstate)) {
                BlockPos blockpos1 = /*here different from FlowingFluid*/ isGas ? blockpos.above() : blockpos.below();
                boolean flag = short2booleanmap.computeIfAbsent(short1, (p_192903_) ->
                {
                    BlockState blockstate1 = p_256191_.getBlockState(blockpos1);
                    return this.isWaterHole(p_256191_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                });
                int j;
                if (flag) {
                    j = 0;
                } else {
                    j = this.getSlopeDistance(p_256191_, blockpos, 1, direction.getOpposite(), blockstate, p_76081_, short2objectmap, short2booleanmap);
                }

                if (j < i) {
                    map.clear();
                }

                if (j <= i) {
                    map.put(direction, fluidstate1);
                    i = j;
                }
            }
        }

        return map;
    }

    @Override
    protected boolean isSolidFace(BlockGetter p_75991_, BlockPos p_75992_, Direction p_75993_) {
        BlockState blockstate = p_75991_.getBlockState(p_75992_);
        FluidState fluidstate = p_75991_.getFluidState(p_75992_);
        if (fluidstate.getType().isSame(this)) {
            return false;
        } else if (p_75993_ == /*here different from FlowingFluid*/ (isGas ? Direction.DOWN : Direction.UP)) {
            return true;
        } else {
            return blockstate.getBlock() instanceof IceBlock ? false : blockstate.isFaceSturdy(p_75991_, p_75992_, p_75993_);
        }
    }

    // @Override
    protected boolean isWaterHole(BlockGetter p_75957_, Fluid p_75958_, BlockPos p_75959_, BlockState p_75960_, BlockPos p_75961_, BlockState p_75962_) {
        if (!this.canPassThroughWall(isGas ? Direction.UP : Direction.DOWN, p_75957_, p_75959_, p_75960_, p_75961_, p_75962_)) {
            return false;
        } else {
            return p_75962_.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(p_75957_, p_75961_, p_75962_, p_75958_);
        }
    }

    /** To protect water block */
    @Override
    protected boolean canHoldFluid(BlockGetter p_75973_, BlockPos p_75974_, BlockState p_75975_, Fluid p_75976_) {
        // Calen: for oil spread on water and not replace water
        if (p_75973_.getFluidState(p_75974_).is(FluidTags.WATER)) {
            return false;
        }

        Block block = p_75975_.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(p_75973_, p_75974_, p_75975_, p_75976_);
        } else if (!(block instanceof DoorBlock) && !p_75975_.is(BlockTags.SIGNS) && !p_75975_.is(Blocks.LADDER) && !p_75975_.is(Blocks.SUGAR_CANE) && !p_75975_.is(Blocks.BUBBLE_COLUMN)) {
            if (!p_75975_.is(Blocks.NETHER_PORTAL) && !p_75975_.is(Blocks.END_PORTAL) && !p_75975_.is(Blocks.END_GATEWAY) && !p_75975_.is(Blocks.STRUCTURE_VOID)) {
                return !p_75975_.blocksMotion();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public VoxelShape getShape(FluidState p_76084_, BlockGetter p_76085_, BlockPos p_76086_) {
        boolean hasSameBelowOrAbove = /*here different from FlowingFluid*/ isGas ? hasSameBelow(p_76084_, p_76085_, p_76086_) : hasSameAbove(p_76084_, p_76085_, p_76086_);
        return p_76084_.getAmount() == 9 && hasSameBelowOrAbove ? Shapes.block() : this.shapes.computeIfAbsent(p_76084_, (p_76073_) ->
        {
            if (isGas) {
                return Shapes.box(0.0D, 1.0D - p_76073_.getOwnHeight(), 0.0D, 1.0D, 1.0D, 1.0D);
            } else {
                return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double) p_76073_.getHeight(p_76085_, p_76086_), 1.0D);
            }
        });
    }

    public float getBottomHeight(FluidState p_76050_, BlockGetter p_76051_, BlockPos p_76052_) {
        return hasSameBelow(p_76050_, p_76051_, p_76052_) ? 0.0F : 1.0F - p_76050_.getOwnHeight();
    }

    private static boolean hasSameBelow(FluidState p_76089_, BlockGetter p_76090_, BlockPos p_76091_) {
        return p_76089_.getType().isSame(p_76090_.getFluidState(p_76091_.below()).getType());
    }

    @Override
    public float getHeight(FluidState p_76050_, BlockGetter p_76051_, BlockPos p_76052_) {
        if (isGas) {
            return 1.0F;
        } else {
            return hasSameAbove(p_76050_, p_76051_, p_76052_) ? 1.0F : p_76050_.getOwnHeight();
        }
    }
}
