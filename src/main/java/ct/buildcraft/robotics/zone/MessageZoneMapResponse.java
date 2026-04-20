/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.robotics.zone;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageZoneMapResponse {
    private ZonePlannerMapChunkKey key;
    private ZonePlannerMapChunk data;

    @SuppressWarnings("unused")
    public MessageZoneMapResponse() {
    }

    public MessageZoneMapResponse(ZonePlannerMapChunkKey zonePlannerMapChunkKey, ZonePlannerMapChunk data) {
        this.key = zonePlannerMapChunkKey;
        this.data = data;
    }

    public MessageZoneMapResponse(FriendlyByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
        data = new ZonePlannerMapChunk(new FriendlyByteBuf(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        key.toBytes(buf);
        data.write(new FriendlyByteBuf(buf));
    }

    public static final BiConsumer<MessageZoneMapResponse, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ZonePlannerMapDataClient.INSTANCE.onChunkReceived(message.key, message.data);
        ctx.get().setPacketHandled(true);
    };
}
