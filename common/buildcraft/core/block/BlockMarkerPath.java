/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by lib.block.BlockMarkerBase (not yet migrated to Fabric 1.20.1)
package buildcraft.core.block;

import net.minecraft.block.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.tile.TileMarkerPath;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World worldIn, BlockState state) {
        return new TileMarkerPath();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClient) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileMarkerPath) {
                TileMarkerPath marker = (TileMarkerPath) tile;
                if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, marker.getPermBlock())) {
                    marker.reverseDirection();
                }
            }
        }
        return true;
    }
}
