/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IForgeShearable;

import java.util.List;

public enum StripesHandlerShears implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          PlayerEntity player,
                          IStripesActivator activator) {
        if (!(stack.getItem() instanceof ShearsItem)) {
            return false;
        }

        pos = pos.relative(direction);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

//        if (block instanceof IShearable shearableBlock)
        if (block instanceof IForgeShearable) {
            IForgeShearable shearableBlock = (IForgeShearable) block;
            if (shearableBlock.isShearable(stack, world, pos)) {
                List<ItemStack> drops = shearableBlock.onSheared(null, stack, world, pos, 0);
//                if (stack.attemptDamageItem(1, player.getRNG(), player instanceof ServerPlayerEntity ? (ServerPlayerEntity) player : null))
                if (stack.hurt(1, player.getRandom(), player instanceof ServerPlayerEntity ? (ServerPlayerEntity) player : null)) {
                    stack.shrink(1);
                }
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), BlockConstants.UPDATE_ALL_IMMEDIATE); // Might become obsolete in 1.12+
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction);
                }
                return true;
            }
        }
        return false;
    }
}
