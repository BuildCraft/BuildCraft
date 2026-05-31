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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileBuilder;

public class BlockBuilder extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = BuildCraftProperties.SNAPSHOT_TYPE;

    public BlockBuilder(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(SNAPSHOT_TYPE);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        BlockEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBuilder) {
            return state
                    .withProperty(
                            SNAPSHOT_TYPE,
                            EnumOptionalSnapshotType.fromNullable(((TileBuilder) tile).snapshotType)
                    );
        }
        return state;
    }

    // Others

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileBuilder();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.BUILDER.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(World world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getTileEntity(pos);
        return !(tile instanceof TileBuilder) || ((TileBuilder) tile).getBuilder() == null;
    }
}
