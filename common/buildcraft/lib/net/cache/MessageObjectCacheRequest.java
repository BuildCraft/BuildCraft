/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net.cache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.net.PacketBufferBC;

/**
 * Signifies a client to server request for the value of a cached object, given its ID.
 */
public class MessageObjectCacheRequest implements IMessage {

    private int cacheId;

    private int[] ids;

    @SuppressWarnings("unused")
    public MessageObjectCacheRequest() {
    }

    MessageObjectCacheRequest(NetworkedObjectCache<?> cache, int[] ids) {
        this.cacheId = BuildCraftObjectCaches.CACHES.indexOf(cache);
        this.ids = ids;
        if (ids.length > Short.MAX_VALUE) {
            throw new IllegalStateException("Tried to request too many ID's! (" + ids.length + ")");
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(cacheId);
        buf.writeShort(ids.length);
        for (int id : ids) {
            buf.writeInt(id);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        cacheId = buf.readByte();
        int idCount = buf.readShort();
        ids = new int[idCount];
        for (int i = 0; i < idCount; i++) {
            ids[i] = buf.readInt();
        }
    }

    public static final IMessageHandler<MessageObjectCacheRequest, MessageObjectCacheResponse> HANDLER = (message, ctx) -> {
        NetworkedObjectCache<?> cache = BuildCraftObjectCaches.CACHES.get(message.cacheId);
        byte[][] values = new byte[message.ids.length][];

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        for (int i = 0; i < values.length; i++) {
            int id = message.ids[i];
            cache.writeObjectServer(id, buffer);
            values[i] = new byte[buffer.readableBytes()];
            buffer.readBytes(values[i]);
            buffer.clear();
        }
        return new MessageObjectCacheResponse(message.cacheId, message.ids, values);
    };
}
