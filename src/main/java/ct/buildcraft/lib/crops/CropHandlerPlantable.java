/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.crops;

import ct.buildcraft.api.crops.ICropHandler;
import ct.buildcraft.lib.misc.BlockUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MelonBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

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
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (seed.getItem() instanceof IPlantable) {
            Block block = state.getBlock();
            return block.canSustainPlant(state, world, pos, Direction.UP, (IPlantable) seed.getItem()) && world.isEmptyBlock(pos.above());
        } else {
            Block block = state.getBlock();
            IPlantable plantable = (IPlantable) ((BlockItem) seed.getItem()).getBlock();
            return block.canSustainPlant(state, world, pos, Direction.UP, plantable) && block != ((BlockItem) seed.getItem()).getBlock() && world.isEmptyBlock(pos.above());
        }
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        return BlockUtil.useItemOnBlock(world, player, seed, pos, Direction.UP);
    }

    @Override
    public boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof MelonBlock || block instanceof MushroomBlock || block instanceof DoublePlantBlock
            || block == Blocks.PUMPKIN) {
            return true;
        } else if (block instanceof CropBlock) {
            return ((CropBlock) block).isMaxAge(state);
        } else if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) == 3;
        } else if (block instanceof IPlantable) {
            if (blockAccess.getBlockState(pos.below()).getBlock() == block) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
//        if (!world.isRemote) {
//            BlockState state = world.getBlockState(pos);
//            if (BlockUtil.breakBlock((LevelLevel) world, pos, drops, pos)) {
//                SoundUtil.playBlockBreak(world, pos, state);
//                return true;
//            }
//        }
        return false;
    }
}
