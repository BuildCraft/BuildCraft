/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.block;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.builders.BCBuildersBlocks;
import ct.buildcraft.builders.tile.TileQuarry;
import ct.buildcraft.lib.block.BlockBCTile_Neptune;
import ct.buildcraft.lib.block.IBlockWithFacing;
import ct.buildcraft.lib.misc.AdvancementUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftbuilders:shaping_the_world");

    public BlockQuarry() {
        super(Properties.of(Material.METAL).sound(SoundType.ANVIL).strength(5.0f, 10.0f).requiresCorrectToolForDrops().dynamicShape());
/*        this.registerDefaultState(this.stateDefinition.any()
        		.setValue(BuildCraftProperties.CONNECTED_DOWN, false)
            	.setValue(BuildCraftProperties.CONNECTED_EAST, false)
            	.setValue(BuildCraftProperties.CONNECTED_NORTH, false)
            	.setValue(BuildCraftProperties.CONNECTED_WEST, false)
            	.setValue(BuildCraftProperties.CONNECTED_SOUTH, false)
            	.setValue(BuildCraftProperties.CONNECTED_UP, false));*/
    }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> bs) {
//		BuildCraftProperties.CONNECTED_MAP.values().forEach(bs::add);
		super.createBlockStateDefinition(bs);
	}

/*    private boolean isConnected(LevelAccessor world, BlockPos pos, BlockState state, Direction side) {
        Direction facing = side;
        if (facing.getAxis().isHorizontal()) {
            facing = Direction.from2DDataValue(
                side.get2DDataValue() + 2 + state.getValue(getFacingProperty()).get2DDataValue());
        }
        BlockEntity tile = world.getBlockEntity(pos.offset(facing.getNormal()));
        return tile != null && tile.getCapability(CapUtil.CAP_ITEMS, facing.getOpposite()).isPresent();
    }*/
    

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext bpc) {
//    	Level world = bpc.getLevel();
//    	BlockPos pos = bpc.getClickedPos();
    	BlockState state = this.defaultBlockState();
/*        for (Direction face : Direction.values()) {
            state = state.setValue(BuildCraftProperties.CONNECTED_MAP.get(face), isConnected(world, pos, state, face));
        }*/
        return super.getStateForPlacement(bpc);
	}

    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileQuarry(pos, state);
	}
    
    @Override
    public boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest,
			FluidState fluid) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileQuarry) {
            for (BlockPos blockPos : ((TileQuarry) tile).framePoses) {
                if (world.getBlockState(blockPos).getBlock() == BCBuildersBlocks.FRAME.get()) {
                    world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
		return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
	}



    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileQuarry quarry) {
                List<BlockPos> toRemove = new ArrayList<>(quarry.framePoses);
                if (toRemove.isEmpty() && quarry.frameBox.isInitialized()) {
                    toRemove.addAll(quarry.frameBox.getBlocksOnEdge());
                }
                for (BlockPos blockPos : toRemove) {
                    if (world.getBlockState(blockPos).getBlock() == BCBuildersBlocks.FRAME.get()) {
                        world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        super.onRemove(oldState, world, pos, newState, isMoving);
    }

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
        	AdvancementUtil.unlockAdvancement((Player) placer, ADVANCEMENT);
        }
	}

	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T be) {
		return be instanceof TileQuarry tile ? tile.worldEventListener : null;
	}
	
	
}
