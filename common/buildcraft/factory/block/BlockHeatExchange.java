/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): BlockHeatExchange multiblock connection logic deferred
package buildcraft.factory.block;

import java.util.Locale;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.state.StateManager;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;

import buildcraft.factory.tile.TileHeatExchange;

public class BlockHeatExchange extends BlockBCTile_Neptune implements ICustomPipeConnection, IBlockWithFacing {

    public enum EnumExchangePart implements StringIdentifiable {
        START, MIDDLE, END;

        private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return lowerCaseName;
        }
    }

    public static final Property<EnumExchangePart> PROP_PART = EnumProperty.of("part", EnumExchangePart.class);
    public static final Property<Boolean> PROP_CONNECTED_Y = BooleanProperty.of("connected_y");
    public static final Property<Boolean> PROP_CONNECTED_LEFT = BooleanProperty.of("connected_left");
    public static final Property<Boolean> PROP_CONNECTED_RIGHT = BooleanProperty.of("connected_right");

    public BlockHeatExchange(AbstractBlock.Settings settings, String id) {
        super(settings, id);
        setDefaultState(getStateManager().getDefaultState()
            .with(PROP_PART, EnumExchangePart.MIDDLE)
            .with(PROP_CONNECTED_Y, false)
            .with(PROP_CONNECTED_LEFT, false)
            .with(PROP_CONNECTED_RIGHT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(PROP_PART, PROP_CONNECTED_Y, PROP_CONNECTED_LEFT, PROP_CONNECTED_RIGHT);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileHeatExchange(null, pos, state);
    }

    @Override
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
        return 0;
    }
}
