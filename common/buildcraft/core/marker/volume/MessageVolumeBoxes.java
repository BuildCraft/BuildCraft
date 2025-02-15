/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.net.PacketBufferBC;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageVolumeBoxes implements IMessage {
    private final List<PacketBufferBC> buffers;

    @SuppressWarnings("unused")
    public MessageVolumeBoxes() {
        buffers = new ArrayList<>();
    }

    public MessageVolumeBoxes(List<VolumeBox> volumeBoxes) {
        this.buffers = volumeBoxes.stream()
                .map(volumeBox ->
                {
                    PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
                    volumeBox.toBytes(buffer);
                    return buffer;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeInt(buffers.size());
        for (PacketBufferBC localBuffer : buffers) {
            buf.writeVarInt(localBuffer.readableBytes());
            buf.writeBytes(localBuffer, 0, localBuffer.readableBytes());
        }
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buffers.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            int bytes = buf.readVarInt();
            PacketBufferBC packet = new PacketBufferBC(buf.readBytes(bytes));
            buffers.add(packet);
        }
    }

    public static final IMessageHandler<MessageVolumeBoxes, IMessage> HANDLER = (message, ctx) ->
    {
        Map<PacketBufferBC, VolumeBox> volumeBoxes = message.buffers.stream()
                .map(buffer ->
                {
                    VolumeBox volumeBox;
                    try {
                        volumeBox = new VolumeBox(BCLibProxy.getProxy().getClientWorld(), buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    PacketBufferBC buf = new PacketBufferBC(Unpooled.buffer());
                    volumeBox.toBytes(buf);
                    return Pair.of(buf, volumeBox);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(volumeBox -> !volumeBoxes.values().contains(volumeBox));
        for (Map.Entry<PacketBufferBC, VolumeBox> entry : volumeBoxes.entrySet()) {
            boolean wasContained = false;
            for (VolumeBox clientVolumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
                if (clientVolumeBox.equals(entry.getValue())) {
                    try {
                        clientVolumeBox.fromBytes(entry.getKey());
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                    wasContained = true;
                    break;
                }
            }
            if (!wasContained) {
                ClientVolumeBoxes.INSTANCE.volumeBoxes.add(entry.getValue());
                for (Addon addon : entry.getValue().addons.values()) {
                    if (addon != null) {
                        addon.onAdded();
                    }
                }
            }
        }
        return null;
    };
}
