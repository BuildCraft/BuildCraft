/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import ct.buildcraft.lib.debug.ClientDebuggables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class MessageDebugResponse {
    private final List<String> left = new ArrayList<>();
    private final List<String> right = new ArrayList<>();

    public MessageDebugResponse() {}

    public MessageDebugResponse(List<String> left, List<String> right) {
        this.left.addAll(left);
        this.right.addAll(right);
    }

    public static void toBytes(MessageDebugResponse msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.left.size());
        msg.left.forEach(buffer::writeUtf);
        buffer.writeInt(msg.right.size());
        msg.right.forEach(buffer::writeUtf);
    }

    public MessageDebugResponse(FriendlyByteBuf buffer) {
        IntStream.range(0, buffer.readInt())
            .mapToObj(i -> buffer.readUtf())
            .forEach(left::add);
        IntStream.range(0, buffer.readInt())
            .mapToObj(i -> buffer.readUtf())
            .forEach(right::add);
    }

    public static final BiConsumer<MessageDebugResponse, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
    		return;
        ClientDebuggables.SERVER_LEFT.clear();
        ClientDebuggables.SERVER_LEFT.addAll(message.left);
        ClientDebuggables.SERVER_RIGHT.clear();
        ClientDebuggables.SERVER_RIGHT.addAll(message.right);
    	ctx.get().setPacketHandled(true);
    };
}
