/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport;

import buildcraft.api.net.IMessage;
import buildcraft.api.tiles.IBCTileMenuProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkHooks;

@Deprecated()
public enum BCTransportGuis {
    FILTERED_BUFFER,
    PIPE_DIAMOND,
    PIPE_DIAMOND_WOOD,
    PIPE_EMZULI;

    public static final BCTransportGuis[] VALUES = values();

    public static BCTransportGuis get(int id) {
        if (id < 0 || id >= VALUES.length) return null;
        return VALUES[id];
    }

//    public void openGui(EntityPlayer player) {
//        openGui(player, 0, -1, 0);
//    }

//    public void openGui(EntityPlayer player, BlockPos pos) {
//        openGui(player, pos.getX(), pos.getY(), pos.getZ());
//    }

    public void openPipeGui(PlayerEntity player, BlockPos pos, IBCTileMenuProvider holder) {
//        openGui(player, pos.getX(), pos.getY(), pos.getZ());
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
//        player.openGui(BCTransport.INSTANCE, ordinal(), player.getEntityWorld(), x, y, z);
            // Calen 1.18.2: moved from ContainerGate#<init>
            // recreate plug object before gui packed received
            // Client call in BCSiliconMenuTypes#GATE
//            PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());

            IMessage msg = holder.onServerPlayerOpenNoSend(player);
            NetworkHooks.openGui(
                    serverPlayer, holder, buf ->
                    {
                        buf.writeBlockPos(pos);

                        msg.toBytes(buf);
                    }
            );
        }
    }

//    public void openGui(EntityPlayer player, int x, int y, int z) {
//        player.openGui(BCTransport.INSTANCE, ordinal(), player.getEntityWorld(), x, y, z);
//    }
}
