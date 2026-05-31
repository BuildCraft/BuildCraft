/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;

public enum CropHandlerReeds implements ICropHandler {
    INSTANCE;
    public static final int MAX_HEIGHT = 3;

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.REEDS;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block.canSustainPlant(state, world, pos, Direction.UP, Blocks.REEDS) && block != Blocks.REEDS && world.isAirBlock(pos.up());
    }

    @Override
    public boolean plantCrop(World world, PlayerEntity player, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, pos);
    }

    @Override
    public boolean isMature(BlockView access, BlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public boolean harvestCrop(World world, BlockPos pos, DefaultedList<ItemStack> drops) {
        return false;
    }
}
