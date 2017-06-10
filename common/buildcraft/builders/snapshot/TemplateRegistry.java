/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.template.ITemplateHandler;
import buildcraft.api.template.ITemplateRegistry;

public enum TemplateRegistry implements ITemplateRegistry {
    INSTANCE;

    private final EnumMap<EnumHandlerPriority, List<ITemplateHandler>> handlers = new EnumMap<>(EnumHandlerPriority.class);

    TemplateRegistry() {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            handlers.put(priority, new ArrayList<>());
        }
    }

    @Override
    public void addHandler(ITemplateHandler handler, EnumHandlerPriority priority) {
        handlers.get(priority).add(handler);
    }

    @Override
    public boolean handle(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        for (EnumHandlerPriority priority : EnumHandlerPriority.VALUES) {
            for (ITemplateHandler handler : handlers.get(priority)) {
                if (handler.handle(world, pos, player, stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
