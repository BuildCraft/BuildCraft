/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.api.transport.pipe.ICustomPipeConnection;
import ct.buildcraft.factory.tile.TileTank;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTank extends BlockBCTile_Neptune implements ICustomPipeConnection, ITankBlockConnector, EntityBlock {
	private static final BooleanProperty JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
	private static final VoxelShape BOUNDING_BOX = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

	public BlockTank() {
		super(BlockBehaviour.Properties.of(Material.GLASS).sound(SoundType.GLASS).strength(6.0f, 10.0f).requiresCorrectToolForDrops());
		this.registerDefaultState(this.stateDefinition.any().setValue(JOINED_BELOW, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
		bs.add(JOINED_BELOW);
		super.createBlockStateDefinition(bs);
	}

	public VoxelShape getCollisionShape(BlockState p_51176_, BlockGetter p_51177_, BlockPos p_51178_,
			CollisionContext p_51179_) {
		return BOUNDING_BOX;
	}

	public VoxelShape getShape(BlockState p_51171_, BlockGetter p_51172_, BlockPos p_51173_,
			CollisionContext p_51174_) {
		return BOUNDING_BOX;
	}

	public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_,
			CollisionContext p_48738_) {
		return Shapes.empty();
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
		return true;
	}

	@Override
	public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
		return new TileTank(pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
		boolean isTankBelow = bpc.getLevel().getBlockState(bpc.getClickedPos().below())
				.getBlock() instanceof ITankBlockConnector;
		return defaultBlockState().setValue(JOINED_BELOW, isTankBelow);
	}
	
	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof TileTank tile ? 
				tile.tank.getFluid().getFluid().getFluidType().getLightLevel() : 0;//TODOoS
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
			boolean p_60514_) {
		boolean isTankBelow = level.getBlockState(pos.below()
				)
				.getBlock() instanceof ITankBlockConnector;
		level.setBlockAndUpdate(pos, state.setValue(JOINED_BELOW, isTankBelow));
		super.neighborChanged(state.setValue(JOINED_BELOW, isTankBelow), level, pos, block, fromPos, p_60514_);
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState p_60457_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof TileTank) {
			return ((TileTank) tile).getComparatorLevel();
		}
		return 0;
	}

	@Override
	public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
		return face.getAxis() == Axis.Y ? 0 : 2 / 16f;
	}
	
}
