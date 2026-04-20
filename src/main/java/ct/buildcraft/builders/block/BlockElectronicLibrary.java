/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.block;

import ct.buildcraft.builders.tile.TileElectronicLibrary;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
public class BlockElectronicLibrary extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockElectronicLibrary() {
        super();
    }
    
    @Override
	public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
		return new TileElectronicLibrary(pos, state);
	}

    @Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
    	if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileElectronicLibrary tile) {
        	NetworkHooks.openScreen((ServerPlayer)player, tile, pos);
        }
        return InteractionResult.SUCCESS;
	}

        
}
