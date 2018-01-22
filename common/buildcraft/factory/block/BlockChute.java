/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryGuis;
import buildcraft.factory.tile.TileChute;

import javax.annotation.Nullable;

public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<EnumFacing, IProperty<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
        return new TileChute();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCFactoryGuis.CHUTE.openGUI(player, pos);
        }
        return true;
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
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.VALUES) {
            state = state.withProperty(CONNECTED_MAP.get(side), side != state.getValue(getFacingProperty())
                && TileChute.hasInventoryAtPosition(world, pos.offset(side), side));
        }
        return state;
    }

    // IBlockWithFacing

    @Override
    public boolean canFaceVertically() {
        return true;
    }
}
