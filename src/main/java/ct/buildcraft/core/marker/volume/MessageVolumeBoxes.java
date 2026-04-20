/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageVolumeBoxes{
    private final List<FriendlyByteBuf> buffers ;

    public MessageVolumeBoxes() {
        buffers = new ArrayList<>();
    }
    
    public MessageVolumeBoxes(FriendlyByteBuf buf) {
        int count = buf.readInt();
        FriendlyByteBuf[] cache = new FriendlyByteBuf[count];
        for (int i = 0; i < count; i++) {
            int bytes = buf.readVarInt();
            FriendlyByteBuf packet = new FriendlyByteBuf(buf.readBytes(bytes));
            cache[i] = packet;
        }
        buffers = ImmutableList.copyOf(cache);
    }

    public MessageVolumeBoxes(List<VolumeBox> volumeBoxes) {
        this.buffers = volumeBoxes.stream()
            .map(volumeBox -> {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                volumeBox.toBytes(buffer);
                return buffer;
            })
            .collect(Collectors.toList());
    }

    public static void toBytes(MessageVolumeBoxes msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.buffers.size());
        for (FriendlyByteBuf localBuffer : msg.buffers) {
            buf.writeVarInt(localBuffer.readableBytes());
            buf.writeBytes(localBuffer, 0, localBuffer.readableBytes());
        }
    }



    public static final BiConsumer<MessageVolumeBoxes, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        Map<FriendlyByteBuf, VolumeBox> volumeBoxes = message.buffers.stream()
            .map(buffer -> {
                VolumeBox volumeBox;
                try {
                	Minecraft mc = Minecraft.getInstance();
                    volumeBox = new VolumeBox(mc.level, buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                volumeBox.toBytes(buf);
                return Pair.of(buf, volumeBox);
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        ClientVolumeBoxes.INSTANCE.volumeBoxes.removeIf(volumeBox -> !volumeBoxes.values().contains(volumeBox));
        for (Map.Entry<FriendlyByteBuf, VolumeBox> entry : volumeBoxes.entrySet()) {
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
        ctx.get().setPacketHandled(true);
    };
}
