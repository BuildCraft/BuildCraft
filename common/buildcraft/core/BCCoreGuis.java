/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.core.item.ItemList_BC8;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

// Calen: use MessageUtil#serverOpenItemGui
@Deprecated()
public enum BCCoreGuis {
    LIST;

//    public void openGUI(EntityPlayer player) {
//        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
//    }

    //    public void openGUI(EntityPlayer player, BlockPos pos) {
//        player.openGui(BCCore.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
//    }
    public void openGUI(PlayerEntity player, ItemStack stack) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
//            player.openMenu(state.getMenuProvider(player.level, pos));
            Item item = stack.getItem();
            if (item instanceof ItemList_BC8) {
                ItemList_BC8 list = (ItemList_BC8) item;
                NetworkHooks.openGui(serverPlayer, list, serverPlayer.blockPosition());
            } else {
                player.sendMessage(new TranslationTextComponent("buildcraft.error.open_null_menu"), Util.NIL_UUID);
            }
        }
    }
}
