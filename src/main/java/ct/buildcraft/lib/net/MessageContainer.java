/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.lib.gui.MenuBC_Neptune;
import ct.buildcraft.lib.misc.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class MessageContainer {

    private int windowId;
    private int msgId;
    private FriendlyByteBuf payload;

    @SuppressWarnings("unused")
    public MessageContainer() {}

    public MessageContainer(int windowId, int msgId, FriendlyByteBuf payload) {
        this.windowId = windowId;
        this.msgId = msgId;
        this.payload = payload;
    }

    // Packet breakdown:
    // INT - WindowId
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    public MessageContainer(FriendlyByteBuf buf) {
        windowId = buf.readInt();
        msgId = buf.readUnsignedShort();
        int payloadSize = buf.readUnsignedShort();
        payload = new FriendlyByteBuf(buf.readBytes(payloadSize));
    }

    public static void toBytes(MessageContainer msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.windowId);
        buf.writeShort(msg.msgId);
        int length = msg.payload.readableBytes();
        buf.writeShort(length);
        buf.writeBytes(msg.payload, 0, length);
    }

    public static final BiConsumer<MessageContainer, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        try {
            int id = message.windowId;
            NetworkEvent.Context context = ctx.get();
            ServerPlayer per = context.getSender();
            LogicalSide side = context.getDirection().getReceptionSide();
            if (per != null && per.containerMenu.containerId == id && 
            		per.containerMenu instanceof MenuBC_Neptune container) {
            	
                container.readMessage(message.msgId, message.payload, side, context);
                // error checking
                String extra = container.getClass() + ", id = " + container.getIdAllocator().getNameFor(message.msgId);
                MessageUtil.ensureEmpty(message.payload, side == LogicalSide.CLIENT, extra);
            }
            else if(side == LogicalSide.CLIENT && Minecraft.getInstance().player.containerMenu instanceof MenuBC_Neptune container)
            	container.readMessage(message.msgId, message.payload, side, context);
        } catch (IOException e) {
            throw new Error(e);
        } finally {
            //message.payload.release();
        }
    	ctx.get().setPacketHandled(true);
    };

	

}
