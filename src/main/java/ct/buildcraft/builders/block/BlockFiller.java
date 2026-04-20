/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.block;

import ct.buildcraft.builders.tile.TileFiller;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import ct.buildcraft.lib.tile.TileBC_Neptune;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BlockFiller extends BlockBCTile_Neptune implements IBlockWithFacing {
    // public static final IProperty<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    public BlockFiller() {
        super();
        // setDefaultState(getDefaultState().withProperty(PATTERN, EnumFillerPattern.NONE));
    }

    // BlockState
    
    @Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> bs) {
		super.createBlockStateDefinition(bs);
		// bs.add(PATTERN);
	}
/*
	@Override
    public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFiller) {
            TileFiller filler = (TileFiller) tile;
            // return state.withProperty(PATTERN, EnumFillerPattern.NONE); // FIXME
        }
        return state;
    }*/

    // Others

    @Override
	public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
		return new TileFiller(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFiller filler) {
            if (!filler.hasBox()) {
                return InteractionResult.PASS;
            }
            if (!world.isClientSide) {
            	NetworkHooks.openScreen((ServerPlayer)player, filler, pos);
            }
        }
        return InteractionResult.SUCCESS;
	}

    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }
}
