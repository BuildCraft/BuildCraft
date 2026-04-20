/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.list;

import ct.buildcraft.api.items.IList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum ListTooltipHandler {
    INSTANCE;

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && player != null && player.containerMenu instanceof ContainerList) {
            ItemStack list = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!list.isEmpty() && list.getItem() instanceof IList) {
                if (((IList) list.getItem()).matches(list, stack)) {
                    event.getToolTip().add(Component.translatable("tip.list.matches").withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }
}
