/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.tile.TileMiningWell;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class BlockMiningWell extends BlockBCTile_Neptune implements IBlockWithFacing, EntityBlock {

    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    	return new TileMiningWell(pos, state);
	}
    

	@Override
	public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
		return Shapes.INFINITY;
	}

	@Override
	public boolean isCollisionShapeFullBlock(BlockState p_181242_, BlockGetter p_181243_, BlockPos p_181244_) {
		return false;
	}



	@Override
	public VoxelShape getVisualShape(BlockState p_60479_, BlockGetter p_60480_, BlockPos p_60481_,
			CollisionContext p_60482_) {
		return Shapes.INFINITY;
	}



	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> bet) {
		return (bet == BCFactoryBlocks.ENTITYBLOCKMININGWELL.get()) ? ($0,pos,$1,BlockEntity) -> {
			if(BlockEntity instanceof TileMiningWell be)
				be.update();
		} : null;
	}

	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T tile) {
		return tile instanceof TileMiningWell be ? be.worldEventListener :null;
	}
}
