/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.stripes;

import java.util.List;

import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.IStripesHandlerItem;

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

        pos = pos.offset(direction.getNormal());
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof IForgeShearable) {
        	IForgeShearable shearableBlock = (IForgeShearable) block;
            if (shearableBlock.isShearable(stack, world, pos)) {
                List<ItemStack> drops = shearableBlock.onSheared(null, stack, world, pos, 0);
                if (stack.hurt(1, player.getRandom(), player instanceof ServerPlayer ? (ServerPlayer) player : null)) {
                    stack.shrink(1);
                }
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11); // Might become obsolete in 1.12+
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction);
                }
                return true;
            }
        }
        return false;
    }
}
