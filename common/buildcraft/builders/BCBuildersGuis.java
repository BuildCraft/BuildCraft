/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

// Calen: no modGuiId in 1.18.2, so open FILLER_PLANNER gui at another place...
@Deprecated()
public enum BCBuildersGuis {
    ARCHITECT,
    BUILDER,
    FILLER,
    LIBRARY,
    REPLACER,
    FILLER_PLANNER;

    public void openGUI(PlayerEntity player) {
//        player.openGui(BCBuilders.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
        openGUI(player, BlockPos.ZERO);
    }

    public void openGUI(PlayerEntity player, BlockPos pos) {
//        player.openGui(BCBuilders.INSTANCE, ordinal(), player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
//            player.openMenu(state.getMenuProvider(player.level, pos));
            TileEntity te = serverPlayer.level.getBlockEntity(pos);
            if (te instanceof INamedContainerProvider) {
                INamedContainerProvider menuProvider = (INamedContainerProvider) te;
                NetworkHooks.openGui(serverPlayer, menuProvider, pos);
            } else {
                player.sendMessage(new TranslationTextComponent("buildcraft.error.open_null_menu"), Util.NIL_UUID);
            }
        }
    }
}
