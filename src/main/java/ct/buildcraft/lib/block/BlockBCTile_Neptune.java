/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib.block;

import java.util.Collections;
import java.util.List;

import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BlockBCTile_Neptune extends BlockBCBase_Neptune implements EntityBlock {

	
    public BlockBCTile_Neptune(Properties material) {
        super(material);
    }
    
    public BlockBCTile_Neptune() {}

	@Override
	public void wasExploded(Level world, BlockPos pos, Explosion explosion) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onExplode(explosion);
        }
        super.wasExploded(world, pos, explosion);
	}

    @Override
	public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove(true);
        }
		super.onBlockExploded(state, level, pos, explosion);
	}
    

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
			FluidState fluid) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove(!player.isCreative()&&willHarvest);
        }
		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}

    @Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onPlacedBy(placer, stack);
            tileBC.onNeighbourBlockChanged(Blocks.AIR.defaultBlockState(), pos);
            tileBC.neighbourBlockChanged(Blocks.AIR.defaultBlockState(), pos, false);
        }
		super.setPlacedBy(world, pos, state, placer, stack);
	}
    
	@Override
	public List<ItemStack> getDrops(BlockState p_60537_, Builder p_60538_) {
		return Collections.singletonList(new ItemStack(p_60537_.getBlock()));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            return tileBC.onActivated(player, hand, hit);
        }
		return InteractionResult.PASS;
	}


	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
			BlockPos fromPos, boolean harvest) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.neighbourBlockChanged(state, fromPos, harvest);
        }
		super.neighborChanged(state, level, pos, neighbor, fromPos, harvest);

	}
	
	//Only Update for tileEntity changed
	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onNeighbourBlockChanged(state, neighbor);
        }
		super.onNeighborChange(state, level, pos, neighbor);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> bet) {
		return (a,b,c,blockEntity) -> {
			if(blockEntity instanceof TileBC_Neptune tile) {
				tile.update();
			}
		};
	}
	
	
    
    
}
