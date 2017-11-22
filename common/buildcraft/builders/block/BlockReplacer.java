/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileReplacer;

public class BlockReplacer extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockReplacer(Material material, String id) {
        super(material, id);
    }

    @Nullable
    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
        return new TileReplacer();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
        EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.REPLACER.openGUI(player, pos);
        }
        return true;
    }
}
