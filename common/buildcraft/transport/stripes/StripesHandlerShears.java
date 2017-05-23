/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.IShearable;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerShears implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (!(stack.getItem() instanceof ItemShears)) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof IShearable) {
            IShearable shearableBlock = (IShearable) block;
            if (shearableBlock.isShearable(stack, world, pos)) {
                List<ItemStack> drops = shearableBlock.onSheared(stack, world, pos, 0);
                if (stack.attemptDamageItem(1, player.getRNG())) {
                    stack.shrink(1);
                }
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction.getOpposite());
                }
                return true;
            }
        }
        return false;
    }
}
