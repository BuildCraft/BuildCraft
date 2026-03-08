/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.MessageUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;

import java.io.IOException;

public class MessageContainer implements IMessage {

    private int windowId;
    private int msgId;
    private PacketBufferBC payload;

    @SuppressWarnings("unused")
    public MessageContainer() {
    }

    public MessageContainer(int windowId, int msgId, PacketBufferBC payload) {
        this.windowId = windowId;
        this.msgId = msgId;
        this.payload = payload;
    }

    // Packet breakdown:
    // INT - WindowId
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    @Override
    public void fromBytes(PacketBuffer buf) {
        windowId = buf.readInt();
        msgId = buf.readUnsignedShort();
        int payloadSize = buf.readUnsignedShort();
        ByteBuf read = buf.readBytes(payloadSize);
        payload = new PacketBufferBC(read);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(windowId);
        buf.writeShort(msgId);
        int length = payload.readableBytes();
        buf.writeShort(length);
        buf.writeBytes(payload, 0, length);
    }

    public static final IMessageHandler<MessageContainer, IMessage> HANDLER = (message, ctx) ->
    {
        try {
            int id = message.windowId;
            PlayerEntity player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player != null && player.containerMenu instanceof ContainerBC_Neptune<?> && player.containerMenu.containerId == id) {
                ContainerBC_Neptune<?> container = (ContainerBC_Neptune<?>) player.containerMenu;
                container.readMessage(message.msgId, message.payload, ctx.getDirection(), ctx);

                // error checking
                String extra = container.getClass() + ", id = " + container.getIdAllocator().getNameFor(message.msgId);
                MessageUtil.ensureEmpty(message.payload, ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT, extra);
            }
            return null;
        } catch (IOException e) {
            throw new Error(e);
        } finally {
            message.payload.release();
        }
    };
}
