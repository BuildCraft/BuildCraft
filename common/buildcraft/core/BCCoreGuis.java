/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.core.item.ItemList_BC8;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

// Calen: use MessageUtil#serverOpenItemGui
@Deprecated(forRemoval = true)
public enum BCCoreGuis {
    LIST;

//    public void openGUI(EntityPlayer player) {
//        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
//    }

    //    public void openGUI(EntityPlayer player, BlockPos pos) {
//        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
//    }
    public void openGUI(Player player, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
//            player.openMenu(state.getMenuProvider(player.level, pos));
            if (stack.getItem() instanceof ItemList_BC8 list) {
                NetworkHooks.openScreen(serverPlayer, list, serverPlayer.blockPosition());
            } else {
                player.sendSystemMessage(Component.translatable("buildcraft.error.open_null_menu"));
            }
        }
    }
}
