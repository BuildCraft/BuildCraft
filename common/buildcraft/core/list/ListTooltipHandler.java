/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
// STUB(R.Chen): // @SubscribeEvent — TODO(R.Chen): port to Fabric event removed — port to Fabric events

import buildcraft.api.items.IList;

import buildcraft.lib.misc.LocaleUtil;

public enum ListTooltipHandler {
    INSTANCE;

    // @SubscribeEvent — TODO(R.Chen): port to Fabric event
    public void itemTooltipEvent(ItemTooltipEvent event) {
        final PlayerEntity player = event.getEntityPlayer();
        final ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && player != null && player.openContainer instanceof ContainerList) {
            ItemStack list = player.getMainHandStack();
            if (!list.isEmpty() && list.getItem() instanceof IList) {
                if (((IList) list.getItem()).matches(list, stack)) {
                    event.getToolTip().add(Formatting.GREEN + LocaleUtil.localize("tip.list.matches"));
                }
            }
        }
    }
}
