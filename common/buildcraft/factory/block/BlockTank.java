/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.tile.TileTank;

public class BlockTank extends BlockBCTile_Neptune implements ICustomPipeConnection, ITankBlockConnector {
    private static final Property<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    private static final Box BOUNDING_BOX = new Box(2 / 16D, 0 / 16D, 2 / 16D, 14 / 16D, 16 / 16D, 14 / 16D);

    public BlockTank(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileTank();
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(JOINED_BELOW);
    }

    @Environment(EnvType.CLIENT)
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public RenderLayer getBlockLayer() {
        return RenderLayer.getCutout();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isFullCube(BlockState state) {
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public Box getBoundingBox(BlockState state, BlockView world, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Environment(EnvType.CLIENT)
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean shouldSideBeRendered(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return side.getAxis() != Axis.Y || !(world.getBlockState(pos.offset(side)).getBlock() instanceof ITankBlockConnector);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        boolean isTankBelow = world.getBlockState(pos.down()).getBlock() instanceof ITankBlockConnector;
        return state.with(JOINED_BELOW, isTankBelow);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileTank) {
            return ((TileTank) tile).getComparatorLevel();
        }
        return 0;
    }

    @Override
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
        return face.getAxis() == Axis.Y ? 0 : 2 / 16f;
    }
}
