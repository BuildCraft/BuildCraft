/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net.cache;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageObjectCacheResponse {

    private int cacheId;

    private int[] ids;
    private byte[][] values;

    @SuppressWarnings("unused")
    public MessageObjectCacheResponse() {
    }

    MessageObjectCacheResponse(int cacheId, int[] ids, byte[][] values) {
        this.cacheId = cacheId;
        this.ids = ids;
        this.values = values;
    }

    public static void toBytes(MessageObjectCacheResponse msg, ByteBuf buf) {
        buf.writeByte(msg.cacheId);
        buf.writeShort(msg.ids.length);
        for (int i = 0; i < msg.ids.length; i++) {
            buf.writeInt(msg.ids[i]);
            buf.writeShort(msg.values[i].length);
            buf.writeBytes(msg.values[i]);
        }
    }

    public MessageObjectCacheResponse(ByteBuf buf) {
        cacheId = buf.readByte();
        int idCount = buf.readShort();
        ids = new int[idCount];
        values = new byte[idCount][];
        for (int i = 0; i < idCount; i++) {
            ids[i] = buf.readInt();
            values[i] = new byte[buf.readShort()];
            buf.readBytes(values[i]);
        }
    }

    public static final BiConsumer<MessageObjectCacheResponse, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        try {
            NetworkedObjectCache<?> cache = BuildCraftObjectCaches.CACHES.get(message.cacheId);
            for (int i = 0; i < message.ids.length; i++) {
                int id = message.ids[i];
                byte[] payload = message.values[i];
                cache.readObjectClient(id, new FriendlyByteBuf(Unpooled.copiedBuffer(payload)));
            }
        } catch (IOException io) {
            throw new Error(io);
        }
    };
}
