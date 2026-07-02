/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.misc.MessageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class MessageMarker {
    private static final boolean DEBUG = MessageManager.DEBUG;

    public boolean add, multiple, connection;
    public int cacheId, count;
    public final List<BlockPos> positions = new ArrayList<>();

    public MessageMarker() {}
    
    public MessageMarker(FriendlyByteBuf buf) {
        add = buf.readBoolean();
        multiple = buf.readBoolean();
        connection = buf.readBoolean();
        cacheId = buf.readShort();
        if (multiple) {
            count = buf.readShort();
        } else {
            count = 1;
        }
        
        for (int i = 0; i < count; i++) {
            positions.add(MessageUtil.readBlockPos(buf));
        }
    }

    
    public static void toBytes(MessageMarker msg, FriendlyByteBuf buf) {
        msg.count = msg.positions.size();
        msg.multiple = msg.count != 1;
        buf.writeBoolean(msg.add);
        buf.writeBoolean(msg.multiple);
        buf.writeBoolean(msg.connection);
        buf.writeShort(msg.cacheId);
        if (msg.multiple) {
            buf.writeShort(msg.count);
        }
        for (BlockPos pos : msg.positions) {
            MessageUtil.writeBlockPos(buf, pos);
        }
    }

    public MessageMarker copy() {
        MessageMarker copy = new MessageMarker();
        copy.add = add;
        copy.multiple = multiple;
        copy.connection = connection;
        copy.cacheId = cacheId;
        copy.count = count;
        copy.positions.addAll(positions);
        return copy;
    }

    @Override
    public String toString() {
        boolean[] flags = { add, multiple, connection };
        return "Message Marker [" + Arrays.toString(flags) + ", cacheId " + cacheId + ", count = " + count
            + ", positions = " + positions + "]";
    }

    public static final BiConsumer<MessageMarker, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.messages][marker] Received invalid marker message from client!");
            }
            context.setPacketHandled(true);
            return;
        }

        // Marker messages mutate the client marker cache that is read by the render thread. In this port
        // MessageMarker is registered as a bidirectional message, so MessageManager may bypass its normal
        // client-side wrapper and call this handler directly on Netty's network thread. Always hop to the
        // client main thread here; otherwise connection updates can race with MarkerRenderer and leave a
        // half-reset VolumeConnection/Box, which crashes in LaserBoxRenderer or leaves stale blue signal lasers.
        context.enqueueWork(() -> {
            if (!MessageMarkerClientHandler.handleOrQueue(message)) {
                if (DEBUG) {
                    BCLog.logger.warn("[lib.messages][marker] Queued marker message until the client world is ready: " + message);
                }
            }
        });
        context.setPacketHandled(true);
    };
}
