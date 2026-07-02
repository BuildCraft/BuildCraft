/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.stripes;

import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.IStripesHandlerItem;
import ct.buildcraft.api.transport.pipe.IItemPipe;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StripesHandlerPipes implements IStripesHandlerItem {

    @Override
    public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
        if (!(stack.getItem() instanceof IItemPipe)) {
            return false;
        }

        PipeDefinition pipeDefinition = ((IItemPipe) stack.getItem()).getDefinition();
        if (pipeDefinition.flowType == PipeApi.flowItems) {
            // Item pipe: request extending on end of tick
            if (PipeApi.extensionManager.requestPipeExtension(world, pos, direction, activator, stack.copy())) {
                // No items should be sent back immediately, handled by the pipe extension manager later
                player.getInventory().clearContent();
                return true;
            }
        }

        return false;
    }
}
