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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public class BlockHeatExchange extends BlockBCTile_Neptune<TileHeatExchange> implements ICustomPipeConnection, IBlockWithFacing, IBlockWithTickableTE<TileHeatExchange> {

    public enum EnumExchangePart implements IStringSerializable {
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

    public BlockHeatExchange(String idBC, AbstractBlock.Properties props) {
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
    public BlockState getActualState(BlockState state, IWorld world, BlockPos pos, TileEntity tile) {
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
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        TileEntity tile = world.getBlockEntity(pos);
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
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        switch (state.getValue(PROP_PART)) {
            case MIDDLE:
                switch (state.getValue(getFacingProperty()).getAxis()) {
                    case X:
                        return MIDDLE_X;
                    case Z:
                        return MIDDLE_Z;
                    case Y:
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [middle] part never has Y axis!");
                }
            case START:
                switch (state.getValue(getFacingProperty())) {
                    case NORTH:
                        return VoxelShapes.or(START_N_END_S, INNER_TUBE_Z, START_DOWN);
                    case SOUTH:
                        return VoxelShapes.or(START_S_END_N, INNER_TUBE_Z, START_DOWN);
                    case WEST:
                        return VoxelShapes.or(START_W_END_E, INNER_TUBE_X, START_DOWN);
                    case EAST:
                        return VoxelShapes.or(START_E_END_W, INNER_TUBE_X, START_DOWN);
                    default:
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [start] part never has " + state.getValue(getFacingProperty()) + " direction!");
                }
            case END:
                switch (state.getValue(getFacingProperty())) {
                    case NORTH:
                        return VoxelShapes.or(START_S_END_N, INNER_TUBE_Z, END_UP);
                    case SOUTH:
                        return VoxelShapes.or(START_N_END_S, INNER_TUBE_Z, END_UP);
                    case WEST:
                        return VoxelShapes.or(START_E_END_W, INNER_TUBE_X, END_UP);
                    case EAST:
                        return VoxelShapes.or(START_W_END_E, INNER_TUBE_X, END_UP);
                    default:
                        throw new RuntimeException("[factory.heat_exchange] HeatExchange block [end] part never has " + state.getValue(getFacingProperty()) + " direction!");
                }
            default:
                throw new RuntimeException("[factory.heat_exchange] Unexpected HeatExchange block part: [" + state.getValue(PROP_PART) + "]");
        }
    }

    private static boolean doesNeighbourConnect(IWorld world, BlockPos pos, Direction thisFacing, Direction dir) {
        BlockState neighbour = world.getBlockState(pos.relative(dir));
        if (neighbour.getBlock() == BCFactoryBlocks.heatExchange.get()) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
//            return exchange.rotate();
            exchange.rotate();
        }
//        return false;
        return state;
    }

    @Override
    public ActionResultType attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate() ? ActionResultType.PASS : ActionResultType.FAIL;
        }
        return ActionResultType.FAIL;
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileHeatExchange();
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, IBlockReader p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
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
    public float getExtension(World world, BlockPos pos, Direction face, BlockState state) {
        return 0;
    }
}
