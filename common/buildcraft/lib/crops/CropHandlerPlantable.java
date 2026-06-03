/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MelonBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import java.util.List;

import net.minecraftforge.common.IPlantable;

import buildcraft.api.crops.ICropHandler;

import buildcraft.lib.misc.BlockUtil;

public enum CropHandlerPlantable implements ICropHandler {
    INSTANCE;

    @Override
    public boolean isSeed(ItemStack stack) {
        if (stack.getItem() instanceof IPlantable) {
            return true;
        }

        if (stack.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (block instanceof IPlantable && block != Blocks.SUGAR_CANE) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (seed.getItem() instanceof IPlantable) {
            // STUB(R.Chen): Forge Block.canSustainPlant removed — assume sustainable, Phase 10
            return world.isAir(pos.up());
        } else {
            Block block = state.getBlock();
            // STUB(R.Chen): Forge Block.canSustainPlant removed — assume sustainable, Phase 10
            return block != ((BlockItem) seed.getItem()).getBlock() && world.isAir(pos.up());
        }
    }

    @Override
    public boolean plantCrop(World world, PlayerEntity player, ItemStack seed, BlockPos pos) {
        return BlockUtil.useItemOnBlock(world, player, seed, pos, Direction.UP);
    }

    @Override
    public boolean isMature(BlockView blockAccess, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof FlowerBlock || block instanceof TallPlantBlock || block instanceof MelonBlock || block instanceof MushroomPlantBlock || block instanceof TallPlantBlock
            || block == Blocks.PUMPKIN) {
            return true;
        } else if (block instanceof CropBlock) {
            return ((CropBlock) block).isMature(state);
        } else if (block instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) == 3;
        } else if (block instanceof IPlantable) {
            if (blockAccess.getBlockState(pos.down()).getBlock() == block) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean harvestCrop(World world, BlockPos pos, DefaultedList<ItemStack> drops) {
//        if (!world.isClient) {
//            BlockState state = world.getBlockState(pos);
//            if (BlockUtil.breakBlock((ServerWorld) world, pos, drops, pos)) {
//                SoundUtil.playBlockBreak(world, pos, state);
//                return true;
//            }
//        }
        return false;
    }
}
