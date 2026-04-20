/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.block;

import ct.buildcraft.core.blockEntity.TileMarkerVolume;
import ct.buildcraft.lib.block.BlockMarkerBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume() {
        super(Properties.of(Material.DECORATION));
    }

    @Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TileMarkerVolume(p_153215_, p_153216_);
	}


    @Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
			boolean p_60514_) {
    	checkSignalState(level, pos);
	}

    
	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		checkSignalState(world, pos);
	}

    private static void checkSignalState(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerVolume) {
            TileMarkerVolume volume = (TileMarkerVolume) tile;

            boolean powered = world.hasNeighborSignal(pos);

            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }
    
    

    @Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerVolume) {
                TileMarkerVolume volume = (TileMarkerVolume) tile;

                volume.onManualConnectionAttempt(player);
            }
        }
        return InteractionResult.SUCCESS;
	}

}
