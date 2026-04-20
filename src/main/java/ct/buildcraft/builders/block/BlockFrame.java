/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.block;

import java.util.List;
import java.util.Map;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.properties.BuildCraftProperties;
import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.lib.block.BlockBCBase_Neptune;
import ct.buildcraft.transport.block.BlockPipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final VoxelShape BASE_AABB = BlockPipeHolder.BOX_CENTER;
    public static final VoxelShape[] CONNECTION_AABB = BlockPipeHolder.BOX_FACES;
    

    public BlockFrame() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(0.25f)
				.explosionResistance(3.0f));
        this.registerDefaultState(this.stateDefinition.any()
        		.setValue(BuildCraftProperties.CONNECTED_DOWN, false)
            	.setValue(BuildCraftProperties.CONNECTED_EAST, false)
            	.setValue(BuildCraftProperties.CONNECTED_NORTH, false)
            	.setValue(BuildCraftProperties.CONNECTED_WEST, false)
            	.setValue(BuildCraftProperties.CONNECTED_SOUTH, false)
            	.setValue(BuildCraftProperties.CONNECTED_UP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
    	BuildCraftProperties.CONNECTED_MAP.values().forEach(bs::add);
    	super.createBlockStateDefinition(bs);
    }
    
    

    @Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
    	BlockState state = defaultBlockState();
        for (Direction side : CONNECTED_MAP.keySet()) {
            Block block = bpc.getLevel().getBlockState(bpc.getClickedPos().offset(side.getNormal())).getBlock();
            state = state.setValue(CONNECTED_MAP.get(side), block instanceof BlockFrame || block instanceof BlockQuarry);
        }
        return state;
	}
    
    
    
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			BlockPos fromPos, boolean p_60514_) {
		Block b = level.getBlockState(fromPos).getBlock();
		Direction d = Direction.fromNormal(fromPos.subtract(pos));
		level.setBlockAndUpdate(pos, state.setValue(CONNECTED_MAP.get(d),  b instanceof BlockFrame || b instanceof BlockQuarry ));
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
	public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
		return false;
	}

	
	
    @Override
	public boolean skipRendering(BlockState state, BlockState other, Direction side) {
        Direction[] facings = CONNECTED_MAP.keySet().stream()
                .filter(facing -> state.getValue(CONNECTED_MAP.get(facing)))
                .toArray(Direction[]::new);
        if (facings.length == 1) {
            return side == facings[0];
        } else if (facings.length == 2 && facings[0] == facings[1].getOpposite()) {
            return side == facings[0] || side == facings[1];
        }
        return false;
	}
    
    
    
    @Override
	public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_,
			CollisionContext p_60558_) {
    	Direction[] dirs = Direction.values();
    	Direction[] result = new Direction[6];
    	int len = 0;
    	for(int i = 0;i < 6;i++) {
    		if(state.getValue(CONNECTED_MAP.get(dirs[i])))
    			result[len++] = dirs[i]; 
    	}
    	return BlockPipeHolder.getCachedPipeShape(result, len);
	}

    @Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return List.of();
	}
    


}
