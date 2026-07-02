/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import ct.buildcraft.factory.tile.TileAutoWorkbenchFluids;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BlockAutoWorkbenchFluids extends BlockBCTile_Neptune {

    public BlockAutoWorkbenchFluids() {
        super();
    }

	@Override
	public TileBC_Neptune newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TileAutoWorkbenchFluids(p_153215_, p_153216_);
	}


	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TileAutoWorkbenchFluids tile) {
            NetworkHooks.openScreen((ServerPlayer)player, tile);
        }
        return InteractionResult.SUCCESS;
	}
    
}