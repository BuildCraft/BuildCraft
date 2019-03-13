/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.MessageUtil;

public class MessageContainer implements IMessage {

    private int windowId;
    private int msgId;
    private PacketBufferBC payload;

    @SuppressWarnings("unused")
    public MessageContainer() {}

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
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        msgId = buf.readUnsignedShort();
        int payloadSize = buf.readUnsignedShort();
        ByteBuf read = buf.readBytes(payloadSize);
        payload = new PacketBufferBC(read);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeShort(msgId);
        int length = payload.readableBytes();
        buf.writeShort(length);
        buf.writeBytes(payload, 0, length);
    }

    public static final IMessageHandler<MessageContainer, IMessage> HANDLER = (message, ctx) -> {
        try {
            int id = message.windowId;
            EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(ctx);
            if (player != null && player.openContainer instanceof ContainerBC_Neptune
                && player.openContainer.windowId == id) {
                ContainerBC_Neptune container = (ContainerBC_Neptune) player.openContainer;
                container.readMessage(message.msgId, message.payload, ctx.side, ctx);

                // error checking
                String extra = container.getClass() + ", id = " + container.getIdAllocator().getNameFor(message.msgId);
                MessageUtil.ensureEmpty(message.payload, ctx.side == Side.CLIENT, extra);
            }
            return null;
        } catch (IOException e) {
            throw new Error(e);
        } finally {
            message.payload.release();
        }
    };
}
