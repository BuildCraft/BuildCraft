/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class MessageZoneMapResponse implements IMessage {
    private ZonePlannerMapChunkKey key;
    private ZonePlannerMapChunk data;

    @SuppressWarnings("unused")
    public MessageZoneMapResponse() {
    }

    public MessageZoneMapResponse(ZonePlannerMapChunkKey zonePlannerMapChunkKey, ZonePlannerMapChunk data) {
        this.key = zonePlannerMapChunkKey;
        this.data = data;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void fromBytes(ByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
        data = new ZonePlannerMapChunk(new PacketByteBuf(buf));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void toBytes(ByteBuf buf) {
        key.toBytes(buf);
        data.write(new PacketByteBuf(buf));
    }

    public static final IMessageHandler<MessageZoneMapResponse, IMessage> HANDLER = (message, ctx) -> {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.data);
        return null;
    };
}
