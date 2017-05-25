/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.IStripesRegistry;

public enum StripesRegistry implements IStripesRegistry {
    INSTANCE;

    private final HandlerPriority[] PRIORITIES = HandlerPriority.values();
    private final EnumMap<HandlerPriority, List<IStripesHandlerItem>> itemHandlers = new EnumMap<>(HandlerPriority.class);
    private final EnumMap<HandlerPriority, List<IStripesHandlerBlock>> blockHandlers = new EnumMap<>(HandlerPriority.class);

    StripesRegistry() {
        for (HandlerPriority priority : PRIORITIES) {
            itemHandlers.put(priority, new ArrayList<>());
            blockHandlers.put(priority, new ArrayList<>());
        }
    }

    @Override
    public void addHandler(IStripesHandlerItem handler) {
        addHandler(handler, HandlerPriority.NORMAL);
    }

    @Override
    public void addHandler(IStripesHandlerItem handler, HandlerPriority priority) {
        itemHandlers.get(priority).add(handler);
    }

    @Override
    public void addHandler(IStripesHandlerBlock handler) {
        addHandler(handler, HandlerPriority.NORMAL);
    }

    @Override
    public void addHandler(IStripesHandlerBlock handler, HandlerPriority priority) {
        blockHandlers.get(priority).add(handler);
    }

    /** @return True if a handler handled the itemstack, false otherwise (and so nothing has been done) */
    @Override
    public boolean handleItem(World world,
                              BlockPos pos,
                              EnumFacing direction,
                              ItemStack stack,
                              EntityPlayer player,
                              IStripesActivator activator) {
        for (HandlerPriority priority : PRIORITIES) {
            for (IStripesHandlerItem handler : itemHandlers.get(priority)) {
                if (handler.handle(world, pos, direction, stack, player, activator)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return True if a handler broke a block, false otherwise (and so nothing has been done) */
    @Override
    public boolean handleBlock(World world,
                               BlockPos pos,
                               EnumFacing direction,
                               EntityPlayer player,
                               IStripesActivator activator) {
        for (HandlerPriority priority : PRIORITIES) {
            for (IStripesHandlerBlock handler : blockHandlers.get(priority)) {
                if (handler.handle(world, pos, direction, player, activator)) {
                    return true;
                }
            }
        }
        return false;
    }
}
