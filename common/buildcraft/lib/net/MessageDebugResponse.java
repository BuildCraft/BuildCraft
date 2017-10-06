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

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import buildcraft.lib.debug.ClientDebuggables;

public class MessageDebugResponse implements IMessage {
    private final List<String> left = new ArrayList<>();
    private final List<String> right = new ArrayList<>();

    public MessageDebugResponse() {}

    public MessageDebugResponse(List<String> left, List<String> right) {
        this.left.addAll(left);
        this.right.addAll(right);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeInt(left.size());
        left.forEach(buf::writeString);
        buf.writeInt(right.size());
        right.forEach(buf::writeString);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        IntStream.range(0, buf.readInt())
            .mapToObj(i -> new PacketBufferBC(buf).readString())
            .forEach(left::add);
        IntStream.range(0, buf.readInt())
            .mapToObj(i -> new PacketBufferBC(buf).readString())
            .forEach(right::add);
    }

    public static final IMessageHandler<MessageDebugResponse, IMessage> HANDLER = (message, ctx) -> {
        ClientDebuggables.SERVER_LEFT.clear();
        ClientDebuggables.SERVER_LEFT.addAll(message.left);
        ClientDebuggables.SERVER_RIGHT.clear();
        ClientDebuggables.SERVER_RIGHT.addAll(message.right);
        return null;
    };
}
