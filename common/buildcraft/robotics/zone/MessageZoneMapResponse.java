/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import net.minecraft.network.FriendlyByteBuf;

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
//    public void fromBytes(ByteBuf buf)
    public void fromBytes(FriendlyByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
//        data = new ZonePlannerMapChunk(new PacketBuffer(buf));
        data = new ZonePlannerMapChunk(new FriendlyByteBuf(buf));
    }

    @Override
//    public void toBytes(ByteBuf buf)
    public void toBytes(FriendlyByteBuf buf) {
        key.toBytes(buf);
//        data.write(new PacketBuffer(buf));
        data.write(new FriendlyByteBuf(buf));
    }

    public static final IMessageHandler<MessageZoneMapResponse, IMessage> HANDLER = (message, ctx) ->
    {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.data);
        return null;
    };
}
