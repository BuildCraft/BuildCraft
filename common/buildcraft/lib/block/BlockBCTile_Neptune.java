/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package buildcraft.lib.block;

import buildcraft.api.tiles.ITickable;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class BlockBCTile_Neptune<T extends BlockEntity> extends BlockBCBase_Neptune implements EntityBlock {

    public BlockBCTile_Neptune(String idBC, BlockBehaviour.Properties props) {
        super(idBC, props);
    }

    @Override
    @Nullable
//    public abstract TileBC_Neptune createTileEntity(World world, IBlockState state);
    public abstract TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state);

//    @Override
//    public boolean hasTileEntity(IBlockState state) {
//        return true;
//    }

    @Override
//    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onExplode(explosion);
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    /** This will be called when BlockState changes. */
    @Override
//    public void breakBlock(World world, BlockPos pos, IBlockState state)
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Calen
        if (!TileBC_Neptune.shouldRefresh(world, pos, state, newState)) {
            return;
        }
        // BC
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove();
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.triggerEvent(eventID, eventParam) : false;
    }

    // Calen: here the block has been set to the new one and the tileEntity has been created
    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onPlacedBy(placer, stack);
        }
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, EntityPlayer player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            return tileBC.onActivated(player, hand, hitResult.getDirection(), hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ());
        }
        return super.use(state, world, pos, player, hand, hitResult);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean p_60514_) {
        super.neighborChanged(state, world, pos, block, fromPos, p_60514_);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune tileBC) {
            tileBC.onNeighbourBlockChanged(block, fromPos);
        }
    }

    // Calen
    @Override
    public final <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> pBlockEntityType) {
        if (this instanceof IBlockWithTickableTE) {
            return (w, p, s, te) -> ((ITickable) te).update();
        } else {
            return null;
        }
    }
}
