/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.crops;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FakePlayerProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public enum CropHandlerReeds implements ICropHandler {
    INSTANCE;
    public static final int MAX_HEIGHT = 3;

    @Override
    public boolean isSeed(ItemStack stack) {
//        return stack.getItem() == Items.REEDS;
        return stack.getItem() == Items.SUGAR_CANE;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
//        return block.canSustainPlant(state, world, pos, Direction.UP, Blocks.REEDS) && block != Blocks.REEDS && world.isAirBlock(pos.up());
        return block.canSustainPlant(state, world, pos, Direction.UP, (SugarCaneBlock) Blocks.SUGAR_CANE) && block != Blocks.SUGAR_CANE && world.isEmptyBlock(pos.above());
    }

    @Override
    public boolean plantCrop(World world, PlayerEntity player, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, pos);
    }

    @Override
    public boolean isMature(IWorld access, BlockState state, BlockPos pos) {
        // return false;
        return state.is(Blocks.SUGAR_CANE) && access.getBlockState(pos.relative(Direction.DOWN)).is(Blocks.SUGAR_CANE);
    }

    @Override
    public CropManager.HarvestResult harvestCrop(World world, BlockPos pos, ItemStack tool, NonNullList<ItemStack> drops) {
        // return false;
        return BlockUtil.harvestBlock((ServerWorld) world, pos, tool, FakePlayerProvider.NULL_PROFILE) ? CropManager.HarvestResult.SUCCESS : CropManager.HarvestResult.FAIL;
    }
}
