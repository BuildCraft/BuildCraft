/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageMarker implements IMessage {
    private static final boolean DEBUG = MessageManager.DEBUG;

    public boolean add, multiple, connection;
    public int cacheId, count;
    public final List<BlockPos> positions = new ArrayList<>();

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        PacketBufferBC packet = PacketBufferBC.asPacketBufferBc(buf);
        add = packet.readBoolean();
        multiple = packet.readBoolean();
        connection = packet.readBoolean();
        cacheId = packet.readShort();
        if (multiple) {
            count = packet.readShort();
        } else {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            positions.add(MessageUtil.readBlockPos(packet));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        count = positions.size();
        multiple = count != 1;
        PacketBufferBC packet = PacketBufferBC.asPacketBufferBc(buf);
        packet.writeBoolean(add);
        packet.writeBoolean(multiple);
        packet.writeBoolean(connection);
        packet.writeShort(cacheId);
        if (multiple) {
            packet.writeShort(count);
        }
        for (BlockPos pos : positions) {
            MessageUtil.writeBlockPos(packet, pos);
        }
    }

    @Override
    public String toString() {
        boolean[] flags = { add, multiple, connection };
        return "Message Marker [" + Arrays.toString(flags) + ", cacheId " + cacheId + ", count = " + count
                + ", positions = " + positions + "]";
    }

    public static final IMessageHandler<MessageMarker, IMessage> HANDLER = (message, ctx) ->
    {
        Level world = BCLibProxy.getProxy().getClientWorld();
        if (world == null) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.messages][marker] The world was null for a message!");
            }
            return null;
        }
        if (message.cacheId < 0 || message.cacheId >= MarkerCache.CACHES.size()) {
            if (DEBUG) {
                BCLog.logger.warn("[lib.messages][marker] The cache ID " + message.cacheId + " was invalid!");
            }
            return null;
        }
        MarkerCache<?> cache = MarkerCache.CACHES.get(message.cacheId);
        cache.getSubCache(world).handleMessageMain(message);
        return null;
    };
}
