/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.robotics.RoboticsGuis;
import buildcraft.robotics.tile.TileZonePlanner;

public class BlockZonePlanner extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockZonePlanner(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileZonePlanner();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            RoboticsGuis.ZONE_PLANTER.openGUI(player, pos);
        }
        return true;
    }
}
