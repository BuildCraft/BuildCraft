/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.transport.BCTransportRegistries;

public class StripesHandlerPipes implements IStripesHandlerItem {

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (!(stack.getItem() instanceof IItemPipe)) {
            return false;
        }

        PipeDefinition pipeDefinition = ((IItemPipe) stack.getItem()).getDefinition();
        if (pipeDefinition.flowType == PipeApi.flowItems) {
            // Item pipe: request extending on end of tick
            if (BCTransportRegistries.extensionManager.requestPipeExtension(world, pos, direction, activator, stack.copy())) {
                // No items should be sent back immediately, handled by the pipe extension manager later
                player.inventory.clear();
                return true;
            }
        }

        return false;
    }
}
