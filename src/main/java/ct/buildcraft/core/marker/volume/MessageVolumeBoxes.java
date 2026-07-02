/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import net.minecraftforge.fml.DistExecutor;

import net.minecraftforge.api.distmarker.Dist;

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
    final List<FriendlyByteBuf> buffers ;

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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MessageVolumeBoxesClientHandler.handle(message, ctx));
    };
}
