/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.block;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.builders.tile.TileArchitectTable;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BlockArchitectTable extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final BooleanProperty PROP_VALID = BuildCraftProperties.VALID;

    public BlockArchitectTable() {
        super();
        registerDefaultState(stateDefinition.any().setValue(PROP_VALID, Boolean.TRUE));
    }

    
    @Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> bs) {
		super.createBlockStateDefinition(bs);
		bs.add(PROP_VALID);
	}
    
    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileArchitectTable(pos, state);
	}

    @Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
        if (!world.isClientSide&&world.getBlockEntity(pos) instanceof TileArchitectTable tile) {
        	NetworkHooks.openScreen((ServerPlayer)player, tile, pos);
        }
        return InteractionResult.SUCCESS;
	}


    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }
}
