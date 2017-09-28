/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.misc.MessageUtil;

public class MessageZoneMapRequest implements IMessage {
    private ZonePlannerMapChunkKey key;

    @SuppressWarnings("unused")
    public MessageZoneMapRequest() {
    }

    public MessageZoneMapRequest(ZonePlannerMapChunkKey key) {
        this.key = key;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        key = new ZonePlannerMapChunkKey(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        key.toBytes(buf);
    }

    public static final IMessageHandler<MessageZoneMapRequest, IMessage> HANDLER = (message, ctx) -> {
        MessageUtil.sendReturnMessage(
                ctx,
                new MessageZoneMapResponse(
                        message.key,
                        ZonePlannerMapDataServer.INSTANCE.getChunk(
                                ctx.getServerHandler().player.world,
                                message.key
                        )
                )
        );
        return null;
    };
}
