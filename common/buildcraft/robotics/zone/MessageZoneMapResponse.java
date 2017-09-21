/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketBuffer;

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

    @Override
    public void fromBytes(ByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
        data = new ZonePlannerMapChunk(new PacketBuffer(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        key.toBytes(buf);
        data.write(new PacketBuffer(buf));
    }

    public static final IMessageHandler<MessageZoneMapResponse, IMessage> HANDLER = (message, ctx) -> {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.data);
        return null;
    };
}
