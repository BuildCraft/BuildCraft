/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockFloodGate extends BlockBCTile_Neptune {
    public static final Map<EnumFacing, IProperty<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(EnumFacing.UP);
    }

    public BlockFloodGate(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileFloodGate();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = BlockUtil.getTileEntityForGetActualState(world, pos);
        if (tile instanceof TileFloodGate) {
            for (EnumFacing side : CONNECTED_MAP.keySet()) {
                state = state.withProperty(CONNECTED_MAP.get(side), ((TileFloodGate) tile).openSides.get(side));
            }
        }
        return state;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            if (!world.isRemote) {
                if (side != EnumFacing.UP) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileFloodGate) {
                        if (CONNECTED_MAP.containsKey(side)) {
                            ((TileFloodGate) tile).openSides.put(side, !((TileFloodGate) tile).openSides.get(side));
                            ((TileFloodGate) tile).queue.clear();
                            ((TileFloodGate) tile).sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
