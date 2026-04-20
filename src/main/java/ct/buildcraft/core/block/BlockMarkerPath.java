/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.core.block;

import ct.buildcraft.core.blockEntity.TileMarkerPath;
import ct.buildcraft.lib.block.BlockMarkerBase;
import ct.buildcraft.lib.misc.PermissionUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath() {
        super(Properties.of(Material.DECORATION));
    }
    
    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileMarkerPath(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerPath) {
                TileMarkerPath marker = (TileMarkerPath) tile;
                if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, marker.getPermBlock())) {
                    marker.reverseDirection();
                }
            }
        }
        return InteractionResult.SUCCESS;
	}

}
