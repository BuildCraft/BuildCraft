/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.factory.tile.TileMiner;
import ct.buildcraft.factory.tile.TilePump;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;
import ct.buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTube extends BlockBCBase_Neptune {
    private static final VoxelShape BOUNDING_BOX = Block.box(4D, 0D, 4D, 12D, 16D, 12D);

    public BlockTube() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(-1.0F, 3600000.0F).noLootTable());
    }

    @Override
	public boolean isCollisionShapeFullBlock(BlockState p_181242_, BlockGetter p_181243_, BlockPos p_181244_) {
    	return false;
    }

	@Override
	public boolean isOcclusionShapeFullBlock(BlockState p_222959_, BlockGetter p_222960_, BlockPos p_222961_) {
		return false;
	}

    @Override
	public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest,
			FluidState fluid) {
    	BlockPos currentPos = pos;
    	// noinspection StatementWithEmptyBody
        while (world.getBlockState(currentPos = currentPos.above()).getBlock() == this) {
        }
        if (!(world.getBlockEntity(currentPos) instanceof TileMiner)) {
            return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        return false;
    	
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos,
			CollisionContext context) {
		return BOUNDING_BOX;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			BlockPos fromPos, boolean p_60514_) {
		if(pos.getY() - 1 == fromPos.getY() && BlockUtil.getFluid(block) != Fluids.EMPTY && block != level.getBlockState(fromPos).getBlock()) {
			BlockPos p = pos.above();
			while(true) {
				BlockEntity be = level.getBlockEntity(p);
				if(be instanceof TilePump pump) {
					pump.neighbourBlockChanged(level.getBlockState(p), fromPos, true);
					BCLog.logger.debug("111");
					break;
				}
				if(be instanceof TileMiner ) {
					break;
				}
				p = p.above();
			}
		}
		super.neighborChanged(state, level, pos, block, fromPos, p_60514_);
	}

	
    
}
