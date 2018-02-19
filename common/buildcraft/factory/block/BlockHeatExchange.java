/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.BCBlocks;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class BlockHeatExchange extends BlockBCTile_Neptune implements ICustomPipeConnection, IBlockWithFacing {

    public enum EnumExchangePart implements IStringSerializable {
        START,
        MIDDLE,
        END;

        private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getName() {
            return lowerCaseName;
        }
    }

    public static final IProperty<EnumExchangePart> PROP_PART = PropertyEnum.create("part", EnumExchangePart.class);
    public static final IProperty<Boolean> PROP_CONNECTED_Y = PropertyBool.create("connected_y");
    public static final IProperty<Boolean> PROP_CONNECTED_LEFT = PropertyBool.create("connected_left");
    public static final IProperty<Boolean> PROP_CONNECTED_RIGHT = PropertyBool.create("connected_right");

    public BlockHeatExchange(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_PART);
        properties.add(PROP_CONNECTED_Y);
        properties.add(PROP_CONNECTED_LEFT);
        properties.add(PROP_CONNECTED_RIGHT);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            EnumExchangePart part;
            if (exchange.isStart()) {
                part = EnumExchangePart.START;
            } else if (exchange.isEnd()) {
                part = EnumExchangePart.END;
            } else {
                part = EnumExchangePart.MIDDLE;
            }
            EnumFacing thisFacing = state.getValue(PROP_FACING);
            state = state.withProperty(PROP_PART, part);
            state = state.withProperty(PROP_CONNECTED_Y, false);

            boolean connectLeft = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateY());
            state = state.withProperty(PROP_CONNECTED_LEFT, connectLeft);

            boolean connectRight = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateYCCW());
            state = state.withProperty(PROP_CONNECTED_RIGHT, connectRight);
        }
        state = state.withProperty(PROP_CONNECTED_Y, false);
        return state;
    }

    private static boolean doesNeighbourConnect(IBlockAccess world, BlockPos pos, EnumFacing thisFacing,
        EnumFacing dir) {
        IBlockState neighbour = world.getBlockState(pos.offset(dir));
        return neighbour.getBlock() == BCBlocks.Factory.HEAT_EXCHANGE && neighbour.getValue(PROP_FACING) == thisFacing;
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
        return new TileHeatExchange();
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        return 0;
    }
}
