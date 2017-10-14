/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.net.PacketBufferBC;

public class MessageVolumeBoxes implements IMessage {
    private final List<PacketBufferBC> boxes;

    @SuppressWarnings("unused")
    public MessageVolumeBoxes() {
        boxes = new ArrayList<>();
    }

    public MessageVolumeBoxes(List<VolumeBox> boxes) {
        this.boxes = boxes.stream().map(box -> {
            PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
            box.toBytes(buffer);
            return buffer;
        }).collect(Collectors.toList());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeInt(boxes.size());
        for (PacketBufferBC buf2 : boxes) {
            buf.writeVarInt(buf2.readableBytes());
            buf.writeBytes(buf2, 0, buf2.readableBytes());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        boxes.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            int bytes = buf.readVarInt();
            PacketBufferBC packet = new PacketBufferBC(buf.readBytes(bytes));
            boxes.add(packet);
        }
    }

    public static final IMessageHandler<MessageVolumeBoxes, IMessage> HANDLER = (message, ctx) -> {
        ClientVolumeBoxes.INSTANCE.boxes.removeIf(box -> !message.boxes.contains(box));
        try {
            for (PacketBufferBC packet : message.boxes) {
                VolumeBox box = new VolumeBox(BCLibProxy.getProxy().getClientWorld(), packet);
                PacketBufferBC buf = new PacketBufferBC(Unpooled.buffer());
                box.toBytes(buf);
                boolean wasContained = false;
                for (VolumeBox clientBox : ClientVolumeBoxes.INSTANCE.boxes) {
                    if (clientBox.equals(box)) {
                        try {
                            clientBox.fromBytes(buf);
                        } catch (IOException io) {
                            throw new RuntimeException(io);
                        }
                        wasContained = true;
                        break;
                    }
                }
                if (!wasContained) {
                    ClientVolumeBoxes.INSTANCE.boxes.add(box);
                    for (Addon addon : box.addons.values()) {
                        if (addon != null) {
                            addon.onAdded();
                        }
                    }
                }
            }
            return null;
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    };
}
