/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net.cache;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.lib.net.MessageManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Signifies a client to server request for the value of a cached object, given
 * its ID.
 */
public class MessageObjectCacheRequest {

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

	public static void toBytes(MessageObjectCacheRequest msg, FriendlyByteBuf buf) {
		buf.writeByte(msg.cacheId);
		buf.writeShort(msg.ids.length);
		for (int id : msg.ids) {
			buf.writeInt(id);
		}
	}

	public MessageObjectCacheRequest(FriendlyByteBuf buf) {
		cacheId = buf.readByte();
		int idCount = buf.readShort();
		ids = new int[idCount];
		for (int i = 0; i < idCount; i++) {
			ids[i] = buf.readInt();
		}
	}

	public static final BiConsumer<MessageObjectCacheRequest, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
		ctx.get().enqueueWork(() -> {
			NetworkedObjectCache<?> cache = BuildCraftObjectCaches.CACHES.get(message.cacheId);
			byte[][] values = new byte[message.ids.length][];

			FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
			for (int i = 0; i < values.length; i++) {
				int id = message.ids[i];
				cache.writeObjectServer(id, buffer);
				values[i] = new byte[buffer.readableBytes()];
				buffer.readBytes(values[i]);
				buffer.clear();
			}
			MessageManager.sendTo(new MessageObjectCacheResponse(message.cacheId, message.ids, values),
					ctx.get().getSender());
		});
		ctx.get().setPacketHandled(true);
	};
}
