/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.block;

import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.silicon.tile.TileLaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLaser extends BlockBCTile_Neptune implements IBlockWithFacing, EntityBlock {
    protected static final VoxelShape BASE_SHAPE_UP = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);//UP
    protected static final VoxelShape CENTER_SHAPE_UP = Block.box(5D, 4D, 5D, 11D, 13D, 11D);
    protected static final VoxelShape SHAPE_UP = Shapes.or(BASE_SHAPE_UP, CENTER_SHAPE_UP);
    
    protected static final VoxelShape BASE_SHAPE_DOWN = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape CENTER_SHAPE_DOWN = Block.box(5D, 3D, 5D, 11D, 12D, 11D);
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(BASE_SHAPE_DOWN, CENTER_SHAPE_DOWN);
    
    protected static final VoxelShape BASE_SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    protected static final VoxelShape CENTER_SHAPE_EAST = Block.box(4D, 5D, 5D, 13D, 11D, 11D);
    protected static final VoxelShape SHAPE_EAST = Shapes.or(BASE_SHAPE_EAST, CENTER_SHAPE_EAST);
    
    protected static final VoxelShape BASE_SHAPE_WEST = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape CENTER_SHAPE_WEST = Block.box(3D, 5D, 5D, 12D, 11D, 11D);
    protected static final VoxelShape SHAPE_WEST = Shapes.or(BASE_SHAPE_WEST, CENTER_SHAPE_WEST);
    
    protected static final VoxelShape BASE_SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    protected static final VoxelShape CENTER_SHAPE_SOUTH = Block.box(5D, 5D, 4D, 11D, 11D, 13D);
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(BASE_SHAPE_SOUTH, CENTER_SHAPE_SOUTH);
    
    protected static final VoxelShape BASE_SHAPE_NORTH = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape CENTER_SHAPE_NORTH = Block.box(5D, 5D, 3D, 11D, 11D, 12D);
    protected static final VoxelShape SHAPE_NORTH = Shapes.or(BASE_SHAPE_NORTH, CENTER_SHAPE_NORTH);
    
    
    public BlockLaser() {
        super();
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLaser(pos, state);
    }

    @Override
    public boolean canFaceVertically() {
        return true;
    }
    
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter lev, BlockPos pos,
			CollisionContext cc) {
		Direction side = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
		switch(side) {
		case UP:
			return SHAPE_UP;
		case DOWN:
			return SHAPE_DOWN;
		case EAST:
			return SHAPE_EAST;
		case WEST:
			return SHAPE_WEST;
		case SOUTH:
			return SHAPE_SOUTH;
		case NORTH:
			return SHAPE_NORTH;
		}
		return Shapes.block();
	}

    @Override
	public boolean isCollisionShapeFullBlock(BlockState p_181242_, BlockGetter p_181243_, BlockPos p_181244_) {
		return false;
	}
    
	@Override
	public boolean isOcclusionShapeFullBlock(BlockState p_222959_, BlockGetter p_222960_, BlockPos p_222961_) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
		return false;
	}

	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T tile) {
		return tile instanceof GameEventListener listener ? listener : null;
	}
	
	
}
