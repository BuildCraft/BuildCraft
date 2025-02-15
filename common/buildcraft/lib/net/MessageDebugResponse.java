/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import buildcraft.lib.debug.ClientDebuggables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MessageDebugResponse implements IMessage {
    // private final List<String> left = new ArrayList<>();
    private final List<Component> left = new ArrayList<>();
    // private final List<String> right = new ArrayList<>();
    private final List<Component> right = new ArrayList<>();

    public MessageDebugResponse() {
    }

    // public MessageDebugResponse(List<String> left, List<String> right)
    public MessageDebugResponse(List<Component> left, List<Component> right) {
        this.left.addAll(left);
        this.right.addAll(right);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        buf.writeInt(left.size());
//        left.forEach(buf::writeUtf);
        left.forEach(buf::writeComponent);
        buf.writeInt(right.size());
//        right.forEach(buf::writeUtf);
        right.forEach(buf::writeComponent);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        IntStream.range(0, buf.readInt())
//                .mapToObj(i -> new PacketBufferBC(buf).readString())
                .mapToObj(i -> new PacketBufferBC(buf).readComponent())
                .forEach(left::add);
        IntStream.range(0, buf.readInt())
//                .mapToObj(i -> new PacketBufferBC(buf).readString())
                .mapToObj(i -> new PacketBufferBC(buf).readComponent())
                .forEach(right::add);
    }

    public static final IMessageHandler<MessageDebugResponse, IMessage> HANDLER = (message, ctx) ->
    {
        ClientDebuggables.SERVER_LEFT.clear();
        ClientDebuggables.SERVER_LEFT.addAll(message.left);
        ClientDebuggables.SERVER_RIGHT.clear();
        ClientDebuggables.SERVER_RIGHT.addAll(message.right);
        return null;
    };
}
