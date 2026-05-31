/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

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

        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof IPlantable && block != Blocks.REEDS) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (seed.getItem() instanceof IPlantable) {
            Block block = state.getBlock();
            return block.canSustainPlant(state, world, pos, Direction.UP, (IPlantable) seed.getItem()) && world.isAir(pos.up());
        } else {
            Block block = state.getBlock();
            IPlantable plantable = (IPlantable) ((ItemBlock) seed.getItem()).getBlock();
            return block.canSustainPlant(state, world, pos, Direction.UP, plantable) && block != ((ItemBlock) seed.getItem()).getBlock() && world.isAir(pos.up());
        }
    }

    @Override
    public boolean plantCrop(World world, PlayerEntity player, ItemStack seed, BlockPos pos) {
        return BlockUtil.useItemOnBlock(world, player, seed, pos, Direction.UP);
    }

    @Override
    public boolean isMature(BlockView blockAccess, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockMelon || block instanceof BlockMushroom || block instanceof BlockDoublePlant
            || block == Blocks.PUMPKIN) {
            return true;
        } else if (block instanceof BlockCrops) {
            return ((BlockCrops) block).isMaxAge(state);
        } else if (block instanceof BlockNetherWart) {
            return state.getValue(BlockNetherWart.AGE) == 3;
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
