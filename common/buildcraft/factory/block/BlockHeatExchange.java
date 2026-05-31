/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;
import java.util.Locale;

import net.minecraft.block.Material;
import net.minecraft.state.property.Property;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileHeatExchange;

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

    public static final Property<EnumExchangePart> PROP_PART = EnumProperty.create("part", EnumExchangePart.class);
    public static final Property<Boolean> PROP_CONNECTED_Y = BooleanProperty.create("connected_y");
    public static final Property<Boolean> PROP_CONNECTED_LEFT = BooleanProperty.create("connected_left");
    public static final Property<Boolean> PROP_CONNECTED_RIGHT = BooleanProperty.create("connected_right");

    public BlockHeatExchange(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_PART);
        properties.add(PROP_CONNECTED_Y);
        properties.add(PROP_CONNECTED_LEFT);
        properties.add(PROP_CONNECTED_RIGHT);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
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
            Direction thisFacing = state.getValue(PROP_FACING);
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

    private static boolean doesNeighbourConnect(BlockView world, BlockPos pos, Direction thisFacing,
        Direction dir) {
        BlockState neighbour = world.getBlockState(pos.offset(dir));
        if (neighbour.getBlock() == BCFactoryBlocks.heatExchange) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, Direction axis) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate();
        }
        return false;
    }

    @Override
    public ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate() ? ActionResult.PASS : ActionResult.FAIL;
        }
        return ActionResult.FAIL;
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, BlockState state) {
        return new TileHeatExchange();
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public RenderLayer getBlockLayer() {
        return RenderLayer.CUTOUT;
    }

    @Override
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
        return 0;
    }
}
