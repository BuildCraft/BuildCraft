/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTank extends BlockBCTile_Neptune<TileTank> implements ICustomPipeConnection, ITankBlockConnector, SimpleWaterloggedBlock, IBlockWithTickableTE<TileTank> {
    public static final Property<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    private static final Property<Boolean> WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(2 / 16D, 0 / 16D, 2 / 16D, 14 / 16D, 16 / 16D, 14 / 16D);
    private static final VoxelShape BOUNDING_BOX = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public BlockTank(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(JOINED_BELOW, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return BCFactoryBlocks.tankTile.get().create(pos, state);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(JOINED_BELOW);
        properties.add(BlockStateProperties.WATERLOGGED);
    }

    // 1.18.2: moved to BCFactory#clientSetup
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
//    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return BOUNDING_BOX;
    }

    @Override
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        boolean isTankBelow = world.getBlockState(pos.below()).getBlock() instanceof ITankBlockConnector;
        return state.setValue(JOINED_BELOW, isTankBelow);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState oldState = world.getBlockState(pos);
        BlockState newState = super.getStateForPlacement(context);
        boolean isInWater = oldState.getBlock() == Blocks.WATER && oldState.getFluidState().isSource(); // Calen: should be called before #getActualState, or the state will be changed from Water block to Tank block
        newState = getActualState(newState, world, pos, null);
        return newState.setValue(WATERLOGGED, isInWater);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        state = super.updateShape(state, facing, facingState, world, pos, facingPos);
        return getActualState(state, world, pos, null);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    // 1.18.2: ret OPPOSITE value to 1.12.2!
//    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
    public boolean skipRendering(BlockState thisState, BlockState otherState, Direction side) {
        if (otherState.is(this)) {
            return !(side.getAxis() != Axis.Y || !(otherState.getBlock() instanceof ITankBlockConnector));
        }
        return super.skipRendering(thisState, otherState, side);
    }

    @Override
//    public boolean hasComparatorInputOverride(BlockState state)
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
//    public int getComparatorInputOverride(BlockState blockState, Level world, BlockPos pos)
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileTank) {
            return ((TileTank) tile).getComparatorLevel();
        }
        return 0;
    }

    @Override
    public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
        return face.getAxis() == Axis.Y ? 0 : 2 / 16f;
    }

    // SimpleWaterloggedBlock

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
