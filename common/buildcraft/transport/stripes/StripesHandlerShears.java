/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;

import java.util.List;

public enum StripesHandlerShears implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
        if (!(stack.getItem() instanceof ShearsItem)) {
            return false;
        }

        pos = pos.relative(direction);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

//        if (block instanceof IShearable shearableBlock)
        if (block instanceof IForgeShearable shearableBlock) {
            if (shearableBlock.isShearable(stack, world, pos)) {
                List<ItemStack> drops = shearableBlock.onSheared(null, stack, world, pos, 0);
//                if (stack.attemptDamageItem(1, player.getRNG(), player instanceof ServerPlayer ? (ServerPlayer) player : null))
                if (stack.hurt(1, player.getRandom(), player instanceof ServerPlayer ? (ServerPlayer) player : null)) {
                    stack.shrink(1);
                }
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE); // Might become obsolete in 1.12+
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction);
                }
                return true;
            }
        }
        return false;
    }
}
