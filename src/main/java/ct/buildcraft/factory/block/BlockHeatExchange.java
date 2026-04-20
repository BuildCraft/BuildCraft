/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.block;

import java.util.Locale;

import ct.buildcraft.api.transport.pipe.ICustomPipeConnection;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.tile.TileHeatExchange;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockHeatExchange extends BlockBCTile_Neptune implements ICustomPipeConnection, IBlockWithFacing {

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
    public static final BooleanProperty PROP_CONNECTED_Y = BooleanProperty.create("connected_y");
    public static final BooleanProperty PROP_CONNECTED_LEFT = BooleanProperty.create("connected_left");
    public static final BooleanProperty PROP_CONNECTED_RIGHT = BooleanProperty.create("connected_right");
    
    public static final VoxelShape BOUNDING_BOX_WEST = Block.box(2, 2, 0, 14, 14, 16);
    public static final VoxelShape BOUNDING_BOX_NORTH = Block.box(0, 2, 2, 16, 14, 14);

    public BlockHeatExchange() {
        super(Properties.of(Material.GLASS).destroyTime(5.0f)
    			.explosionResistance(10.0f).sound(SoundType.GLASS).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
        		.setValue(PROP_PART, EnumExchangePart.MIDDLE)
        		.setValue(PROP_CONNECTED_Y, false)
        		.setValue(PROP_CONNECTED_LEFT, false)
        		.setValue(PROP_CONNECTED_RIGHT, false));
    }
    
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> properties) {
        properties.add(PROP_PART);
        properties.add(PROP_CONNECTED_Y);
        properties.add(PROP_CONNECTED_LEFT);
        properties.add(PROP_CONNECTED_RIGHT);
		super.createBlockStateDefinition(properties);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
		Level world = bpc.getLevel();
		BlockPos pos = bpc.getClickedPos();
        BlockState state = super.getStateForPlacement(bpc);
        Direction thisFacing = state.getValue(PROP_FACING);

        boolean connectLeft = doesNeighbourConnect(world, pos, thisFacing, thisFacing.getClockWise());
        state = state.setValue(PROP_CONNECTED_LEFT, connectLeft);

        boolean connectRight = doesNeighbourConnect(world, pos, thisFacing, thisFacing.getCounterClockWise());
        state = state.setValue(PROP_CONNECTED_RIGHT, connectRight);
        return state;
	}
	
	

    @Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
			boolean p_60514_) {
        Direction thisFacing = state.getValue(PROP_FACING);

        boolean connectLeft = doesNeighbourConnect(level, pos, thisFacing, thisFacing.getClockWise());
        state = state.setValue(PROP_CONNECTED_LEFT, connectLeft);

        boolean connectRight = doesNeighbourConnect(level, pos, thisFacing, thisFacing.getCounterClockWise());
        state = state.setValue(PROP_CONNECTED_RIGHT, connectRight);
        state = state.setValue(PROP_CONNECTED_Y, false);//CHECK
        level.setBlock(pos, state, 2);
		super.neighborChanged(state, level, pos, block, fromPos, p_60514_);
	}

	private static boolean doesNeighbourConnect(BlockGetter world, BlockPos pos, Direction thisFacing,
        Direction dir) {
        BlockState neighbour = world.getBlockState(pos.offset(dir.getNormal()));
        if (neighbour.getBlock() == BCFactoryBlocks.HEATEXCHANGE_BLOCK.get()) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
	public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation axis) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            exchange.rotate(axis);
            return exchange.getBlockState();
        }
        return state;
	}
    
	@Override
	public TileHeatExchange newBlockEntity(BlockPos pos, BlockState state) {
		return new TileHeatExchange(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_,
			CollisionContext p_60558_) {
		switch(state.getValue(PROP_FACING)) {
		case NORTH:
		case SOUTH:
			return BOUNDING_BOX_NORTH;
		case WEST:
		case EAST:
			return BOUNDING_BOX_WEST;
		default:
			return Shapes.block();
		}
	}

	@Override
	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter p_181243_, BlockPos p_181244_) {
		return false;
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
		return Shapes.empty();
	}

	@Override
	public boolean isOcclusionShapeFullBlock(BlockState p_222959_, BlockGetter p_222960_, BlockPos p_222961_) {
		return false;
	}

	@Override
	public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
		return 0;
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lev, BlockState p_153213_,
			BlockEntityType<T> bet) {
		return bet == BCFactoryBlocks.ENTITYBLOCKHEATEXCHANGE.get() ? ($0,pos,$1,BlockEntity) -> {
			if(BlockEntity instanceof TileHeatExchange) {
				((TileHeatExchange) BlockEntity).update();
			}
		} : null;
	}

}
