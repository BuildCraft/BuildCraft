/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class BlockBCTile_Neptune extends BlockBCBase_Neptune {
    public BlockBCTile_Neptune(Material material, String id) {
        super(material, id);
    }

    @Override
    @Nullable
    public abstract TileBC_Neptune createTileEntity(World world, IBlockState state);

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onExplode(explosion);
        }
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
        ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onPlacedBy(placer, stack);
        }
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            return tileBC.onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
        int fortune) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.addDrops(drops, fortune);
        }
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onNeighbourBlockChanged(block, fromPos);
        }
    }
}
