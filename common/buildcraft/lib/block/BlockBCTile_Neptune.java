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

import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class BlockBCTile_Neptune<T extends TileEntity> extends BlockBCBase_Neptune implements ITileEntityProvider {

    public BlockBCTile_Neptune(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Override
    @Nullable
//    public abstract TileBC_Neptune createTileEntity(World world, IBlockState state);
    public abstract TileBC_Neptune newBlockEntity(IBlockReader world);

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
//    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onExplode(explosion);
        }
        super.onBlockExploded(state, world, pos, explosion);
    }

    /** This will be called when BlockState changes. */
    @Override
//    public void breakBlock(World world, BlockPos pos, IBlockState state)
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Calen
        if (!TileBC_Neptune.shouldRefresh(world, pos, state, newState)) {
            return;
        }
        // BC 1.12.2
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove();
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean triggerEvent(BlockState state, World level, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        final TileEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.triggerEvent(eventID, eventParam) : false;
    }

    // Calen: here the block has been set to the new one and the tileEntity has been created
    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onPlacedBy(placer, stack);
        }
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, EntityPlayer player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            return tileBC.onActivated(player, hand, hitResult.getDirection(), hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ());
        }
        return super.use(state, world, pos, player, hand, hitResult);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean p_60514_) {
        super.neighborChanged(state, world, pos, block, fromPos, p_60514_);
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onNeighbourBlockChanged(block, fromPos);
        }
    }
}
