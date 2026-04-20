/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import ct.buildcraft.api.core.EnumHandlerPriority;
import ct.buildcraft.api.template.ITemplateHandler;
import ct.buildcraft.api.template.ITemplateRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public boolean handle(Level world, BlockPos pos, Player player, ItemStack stack) {
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
