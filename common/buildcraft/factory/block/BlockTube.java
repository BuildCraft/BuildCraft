/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCBase_Neptune;

import buildcraft.factory.tile.TileMiner;

public class BlockTube extends BlockBCBase_Neptune {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 16 / 16D, 12 / 16D);

    public BlockTube(Material material, String id) {
        super(material, id);
        setBlockUnbreakable();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        BlockPos currentPos = pos;
        // noinspection StatementWithEmptyBody
        while (world.getBlockState(currentPos = currentPos.up()).getBlock() == this) {
        }
        return !(world.getTileEntity(currentPos) instanceof TileMiner) && super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }
}
