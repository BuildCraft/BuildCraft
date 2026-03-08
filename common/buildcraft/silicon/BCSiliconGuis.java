/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon;

import buildcraft.lib.net.MessageUpdateTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

@Deprecated(forRemoval = true)
public enum BCSiliconGuis {
    ASSEMBLY_TABLE,
    ADVANCED_CRAFTING_TABLE,
    INTEGRATION_TABLE,
    GATE;

//    public void openGUI(EntityPlayer player) {
//        player.openGui(BCSilicon.INSTANCE, ordinal(), player.getEntityWorld(), 0, 0, 0);
//    }

//    public void openGUI(EntityPlayer player, BlockPos pos) {
//        openGui(player, pos, 0);
//    }

//    public void openGui(EntityPlayer player, BlockPos pos, int data) {
//        int fullId = (data << 8) | ordinal();
//        player.openGui(BCSilicon.INSTANCE, fullId, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
//    }

    // Calen: only for GATE in Server Thread
    public void openGui(Player player, MenuProvider provider, BlockPos pos, int data, MessageUpdateTile msg) {
        int fullId = data << 8;
//        player.openGui(BCSilicon.INSTANCE, fullId, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
        if (player instanceof ServerPlayer serverPlayer) {
//            player.openMenu(state.getMenuProvider(player.level, pos));
//            if (player.level.getBlockEntity(pos) instanceof MenuProvider tile)
//            {
//                NetworkHooks.openGui(serverPlayer, tile, pos);
//            }
            if (this == GATE) {
                NetworkHooks.openGui(
                        serverPlayer, provider, buf ->
                        {
                            buf.writeBlockPos(pos);
                            buf.writeInt(fullId);

                            msg.toBytes(buf);
                        }
                );
            } else {
                NetworkHooks.openGui(serverPlayer, provider, pos);
            }
        }
    }
}
