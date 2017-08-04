/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.net.PacketBufferBC;

public class MessageVolumeBoxes implements IMessage {
    public List<VolumeBox> boxes = new ArrayList<>();

    public MessageVolumeBoxes() {
    }

    public MessageVolumeBoxes(List<VolumeBox> boxes) {
        this.boxes = boxes;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeInt(boxes.size());
        boxes.forEach(box -> box.toBytes(buf));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        boxes.clear();
        int count = buf.readInt();
        try {
            for (int i = 0; i < count; i++) {
                boxes.add(new VolumeBox(buf));
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public static final IMessageHandler<MessageVolumeBoxes, IMessage> HANDLER = (message, ctx) -> {
        ClientVolumeBoxes.INSTANCE.boxes.removeIf(box -> !message.boxes.contains(box));
        message.boxes.stream()
                .filter(box -> !ClientVolumeBoxes.INSTANCE.boxes.contains(box))
                .forEach(ClientVolumeBoxes.INSTANCE.boxes::add);
        for (VolumeBox box : message.boxes) {
            PacketBufferBC buf = new PacketBufferBC(Unpooled.buffer());
            box.toBytes(buf);
            for (VolumeBox clientBox : ClientVolumeBoxes.INSTANCE.boxes) {
                if (clientBox.equals(box)) {
                    try {
                        clientBox.fromBytes(buf);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    break;
                }
            }
        }
        return null;
    };
}
