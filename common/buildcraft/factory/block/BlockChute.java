/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;
import java.util.Map;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryGuis;
import buildcraft.factory.tile.TileChute;

public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileChute();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClient) {
            BCFactoryGuis.CHUTE.openGUI(player, pos);
        }
        return true;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = state.with(CONNECTED_MAP.get(side), side != state.get(getFacingProperty())
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
