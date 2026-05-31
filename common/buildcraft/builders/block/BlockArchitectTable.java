/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileArchitectTable;

public class BlockArchitectTable extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<Boolean> PROP_VALID = BuildCraftProperties.VALID;

    private static final int META_VALID_INDEX = 4;

    public BlockArchitectTable(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PROP_VALID, Boolean.TRUE));
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_VALID);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        BlockState state = super.getStateFromMeta(meta);
        state = state.withProperty(PROP_VALID, (meta & META_VALID_INDEX) == 0);
        return state;
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return super.getMetaFromState(state) | (state.getValue(PROP_VALID) ? 0 : META_VALID_INDEX);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileArchitectTable();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.ARCHITECT.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(World world, BlockPos pos, BlockState state) {
        return false;
    }
}
