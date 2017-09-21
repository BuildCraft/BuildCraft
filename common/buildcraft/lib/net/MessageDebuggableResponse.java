/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.debug.ClientDebuggables;

public class MessageDebuggableResponse implements IMessage {
    private final List<String> left = new ArrayList<>();
    private final List<String> right = new ArrayList<>();

    @SuppressWarnings("unused")
    public MessageDebuggableResponse() {
    }

    @SuppressWarnings("WeakerAccess")
    public MessageDebuggableResponse(List<String> left, List<String> right) {
        this.left.addAll(left);
        this.right.addAll(right);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(left.size());
        left.forEach(new PacketBuffer(buf)::writeString);
        buf.writeInt(right.size());
        right.forEach(new PacketBuffer(buf)::writeString);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        IntStream.range(0, buf.readInt())
            .mapToObj(i -> new PacketBufferBC(buf).readString())
            .forEach(left::add);
        IntStream.range(0, buf.readInt())
            .mapToObj(i -> new PacketBufferBC(buf).readString())
            .forEach(right::add);
    }

    public static final IMessageHandler<MessageDebuggableResponse, IMessage> HANDLER = (message, ctx) -> {
        ClientDebuggables.SERVER_LEFT.clear();
        ClientDebuggables.SERVER_LEFT.addAll(message.left);
        ClientDebuggables.SERVER_RIGHT.clear();
        ClientDebuggables.SERVER_RIGHT.addAll(message.right);
        return null;
    };
}
