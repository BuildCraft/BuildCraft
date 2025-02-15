/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class BlockHeatExchange extends BlockBCTile_Neptune<TileHeatExchange> implements ICustomPipeConnection, IBlockWithFacing, IBlockWithTickableTE<TileHeatExchange> {

    public enum EnumExchangePart implements StringRepresentable {
        START,
        MIDDLE,
        END;

        private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return lowerCaseName;
        }
    }

    public static final EnumProperty<EnumExchangePart> PROP_PART = EnumProperty.create("part", EnumExchangePart.class);
    public static final Property<Boolean> PROP_CONNECTED_Y = BooleanProperty.create("connected_y");
    public static final Property<Boolean> PROP_CONNECTED_LEFT = BooleanProperty.create("connected_left");
    public static final Property<Boolean> PROP_CONNECTED_RIGHT = BooleanProperty.create("connected_right");

    public BlockHeatExchange(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(PROP_PART, EnumExchangePart.MIDDLE)
                        .setValue(PROP_CONNECTED_Y, false)
                        .setValue(PROP_CONNECTED_LEFT, false)
                        .setValue(PROP_CONNECTED_RIGHT, false)
        );
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
    public BlockState getActualState(BlockState state, LevelAccessor world, BlockPos pos, BlockEntity tile) {
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            Direction thisFacing = state.getValue(PROP_FACING);
//            boolean connectLeft = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateY());
            boolean connectLeft = doesNeighbourConnect(world, pos, thisFacing, thisFacing.getClockWise());
            state = state.setValue(PROP_CONNECTED_LEFT, connectLeft);

//            boolean connectRight = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateYCCW());
            boolean connectRight = doesNeighbourConnect(world, pos, thisFacing, thisFacing.getCounterClockWise());
            state = state.setValue(PROP_CONNECTED_RIGHT, connectRight);

            EnumExchangePart part;
            if (exchange.isStart()) {
                part = EnumExchangePart.START;
            } else if (exchange.isEnd()) {
                part = EnumExchangePart.END;
            } else {
                part = EnumExchangePart.MIDDLE;
            }
            state = state.setValue(PROP_PART, part);
            state = state.setValue(PROP_CONNECTED_Y, false);
        }
        state = state.setValue(PROP_CONNECTED_Y, false);
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        BlockEntity tile = world.getBlockEntity(pos);
        return getActualState(state, world, pos, tile);
    }

    // Middle
    private static final VoxelShape MIDDLE_X = Block.box(2, 2, 0, 14, 14, 16);
    private static final VoxelShape MIDDLE_Z = Block.box(0, 2, 2, 16, 14, 14);

    // START/END
    private static final VoxelShape INNER_TUBE_X = Block.box(4, 4, 0, 12, 12, 16);
    private static final VoxelShape INNER_TUBE_Z = Block.box(0, 4, 4, 16, 12, 12);

    private static final VoxelShape START_W_END_E = Block.box(2, 2, 2, 14, 14, 16);
    private static final VoxelShape START_E_END_W = Block.box(2, 2, 0, 14, 14, 14);
    private static final VoxelShape START_S_END_N = Block.box(2, 2, 2, 16, 14, 14);
    private static final VoxelShape START_N_END_S = Block.box(0, 2, 2, 14, 14, 14);

    private static final VoxelShape START_DOWN = Block.box(2, 0, 2, 14, 2, 14);
    private static final VoxelShape END_UP = Block.box(2, 14, 2, 14, 16, 14);

    // Calen: if ret full box, the inner quads will be dark
    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(PROP_PART)) {
            case MIDDLE -> switch (state.getValue(getFacingProperty()).getAxis()) {
                case X -> MIDDLE_X;
                case Z -> MIDDLE_Z;
                case Y ->
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [middle] part never has Y axis!");
            };
            case START -> switch (state.getValue(getFacingProperty())) {
                case NORTH -> Shapes.or(START_N_END_S, INNER_TUBE_Z, START_DOWN);
                case SOUTH -> Shapes.or(START_S_END_N, INNER_TUBE_Z, START_DOWN);
                case WEST -> Shapes.or(START_W_END_E, INNER_TUBE_X, START_DOWN);
                case EAST -> Shapes.or(START_E_END_W, INNER_TUBE_X, START_DOWN);
                default ->
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [start] part never has " + state.getValue(getFacingProperty()) + " direction!");
            };
            case END -> switch (state.getValue(getFacingProperty())) {
                case NORTH -> Shapes.or(START_S_END_N, INNER_TUBE_Z, END_UP);
                case SOUTH -> Shapes.or(START_N_END_S, INNER_TUBE_Z, END_UP);
                case WEST -> Shapes.or(START_E_END_W, INNER_TUBE_X, END_UP);
                case EAST -> Shapes.or(START_W_END_E, INNER_TUBE_X, END_UP);
                default ->
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [end] part never has " + state.getValue(getFacingProperty()) + " direction!");
            };
        };
    }

    private static boolean doesNeighbourConnect(LevelAccessor world, BlockPos pos, Direction thisFacing, Direction dir) {
        BlockState neighbour = world.getBlockState(pos.relative(dir));
        if (neighbour.getBlock() == BCFactoryBlocks.heatExchange.get()) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation direction) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
//            return exchange.rotate();
            exchange.rotate();
        }
//        return false;
        return state;
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate() ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    @Override
//    public TileBC_Neptune createTileEntity(Level world, BlockState state)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileHeatExchange(pos, state);
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }

    // 1.18.2: moved to BCFactory#clientSetup
//    @Override
//    @SideOnly(Side.CLIENT)
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }

    @Override
    public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
        return 0;
    }
}
