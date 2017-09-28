/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.net.PacketBufferBC;

public class MessageObjectCacheResponse implements IMessage {

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

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(cacheId);
        buf.writeShort(ids.length);
        for (int i = 0; i < ids.length; i++) {
            buf.writeInt(ids[i]);
            buf.writeShort(values[i].length);
            buf.writeBytes(values[i]);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
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

    public static final IMessageHandler<MessageObjectCacheResponse, IMessage> HANDLER = (message, ctx) -> {
        try {
            NetworkedObjectCache<?> cache = BuildCraftObjectCaches.CACHES.get(message.cacheId);
            for (int i = 0; i < message.ids.length; i++) {
                int id = message.ids[i];
                byte[] payload = message.values[i];
                cache.readObjectClient(id, new PacketBufferBC(Unpooled.copiedBuffer(payload)));
            }
            return null;
        } catch (IOException io) {
            throw new Error(io);
        }
    };
}
