/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockMarkerBase;

import buildcraft.core.tile.TileMarkerVolume;

public class BlockMarkerVolume extends BlockMarkerBase {
    public BlockMarkerVolume(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMarkerVolume();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        checkSignalState(world, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        checkSignalState(world, pos);
    }

    private static void checkSignalState(World world, BlockPos pos) {
        if (world.isRemote) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMarkerVolume) {
            TileMarkerVolume volume = (TileMarkerVolume) tile;

            boolean powered = world.isBlockPowered(pos);

            if (volume.isShowingSignals() != powered) {
                volume.switchSignals();
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileMarkerVolume) {
                TileMarkerVolume volume = (TileMarkerVolume) tile;

                volume.onManualConnectionAttempt(player);
            }
        }
        return true;
    }
}
