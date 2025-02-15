/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;


public enum CropHandlerReeds implements ICropHandler {
    INSTANCE;
    public static final int MAX_HEIGHT = 3;

    @Override
    public boolean isSeed(ItemStack stack) {
//        return stack.getItem() == Items.REEDS;
        return stack.getItem() == Items.SUGAR_CANE;
    }

    @Override
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
//        return block.canSustainPlant(state, world, pos, Direction.UP, Blocks.REEDS) && block != Blocks.REEDS && world.isAirBlock(pos.up());
        return block.canSustainPlant(state, world, pos, Direction.UP, (SugarCaneBlock) Blocks.SUGAR_CANE) && block != Blocks.SUGAR_CANE && world.isEmptyBlock(pos.above());
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, pos);
    }

    @Override
    public boolean isMature(LevelAccessor access, BlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
        return false;
    }
}
