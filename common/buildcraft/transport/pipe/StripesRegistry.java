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

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.api.transport.IStripesRegistry;

public enum StripesRegistry implements IStripesRegistry {
    INSTANCE;

    private final EnumMap<EnumHandlerPriority, List<IStripesHandlerItem>> itemHandlers = new EnumMap<>(EnumHandlerPriority.class);
    private final EnumMap<EnumHandlerPriority, List<IStripesHandlerBlock>> blockHandlers = new EnumMap<>(EnumHandlerPriority.class);

    StripesRegistry() {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            itemHandlers.put(priority, new ArrayList<>());
            blockHandlers.put(priority, new ArrayList<>());
        }
    }

    @Override
    public void addHandler(IStripesHandlerItem handler, EnumHandlerPriority priority) {
        itemHandlers.get(priority).add(handler);
    }

    @Override
    public void addHandler(IStripesHandlerBlock handler, EnumHandlerPriority priority) {
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
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
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
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            for (IStripesHandlerBlock handler : blockHandlers.get(priority)) {
                if (handler.handle(world, pos, direction, player, activator)) {
                    return true;
                }
            }
        }
        return false;
    }
}
