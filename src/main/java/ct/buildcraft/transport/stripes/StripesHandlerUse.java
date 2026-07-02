/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.IStripesHandlerItem;
import ct.buildcraft.lib.misc.BlockUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public enum StripesHandlerUse implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> ITEMS = new ArrayList<>();

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
        return ITEMS.contains(stack.getItem()) &&
            BlockUtil.useItemOnBlock(
                world,
                player,
                stack,
                pos.offset(direction.getNormal()),
                direction.getOpposite()
            );
    }
}
