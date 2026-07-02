/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.robotics.zone;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.lib.misc.MessageUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageZoneMapRequest {
    private ZonePlannerMapChunkKey key;

    @SuppressWarnings("unused")
    public MessageZoneMapRequest() {
    }

    public MessageZoneMapRequest(ZonePlannerMapChunkKey key) {
        this.key = key;
    }

    public MessageZoneMapRequest(FriendlyByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        key.toBytes(buf);
    }

    public static final BiConsumer<MessageZoneMapRequest, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	ctx.get().enqueueWork(() ->{
        MessageUtil.sendReturnMessage(
                ctx.get(),
                new MessageZoneMapResponse(
                        message.key,
                        ZonePlannerMapDataServer.INSTANCE.getChunk(
                                ctx.get().getSender().level,
                                message.key
                        )
                )
        );
    	});
    	ctx.get().setPacketHandled(true);
    };
}
