/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.network.PacketBuffer;

public class MessageZoneMapRequest implements IMessage {
    private ZonePlannerMapChunkKey key;

    @SuppressWarnings("unused")
    public MessageZoneMapRequest() {
    }

    public MessageZoneMapRequest(ZonePlannerMapChunkKey key) {
        this.key = key;
    }

    @Override
//    public void fromBytes(ByteBuf buf)
    public void fromBytes(PacketBuffer buf) {
        key = new ZonePlannerMapChunkKey(buf);
    }

    @Override
//    public void toBytes(ByteBuf buf)
    public void toBytes(PacketBuffer buf) {
        key.toBytes(buf);
    }

    public static final IMessageHandler<MessageZoneMapRequest, IMessage> HANDLER = (message, ctx) ->
    {
        MessageUtil.sendReturnMessage(
                ctx,
                new MessageZoneMapResponse(
                        message.key,
                        ZonePlannerMapDataServer.INSTANCE.getChunk(
//                                ctx.getServerHandler().player.world,
                                ctx.getSender().level,
                                message.key
                        )
                )
        );
        return null;
    };
}
