/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageWireSystems implements IMessage {
    private Map<Integer, WireSystem> wireSystems = new HashMap<>();

    @SuppressWarnings("unused")
    public MessageWireSystems() {
    }

    public MessageWireSystems(Map<Integer, WireSystem> wireSystems) {
        this.wireSystems = wireSystems;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        FriendlyByteBuf pb = new FriendlyByteBuf(buf);
        pb.writeInt(wireSystems.size());
        wireSystems.forEach((wiresHashCode, wireSystem) ->
        {
            pb.writeInt(wiresHashCode);
            List<WireSystem.WireElement> elements = wireSystem.elements.stream()
                    .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
                    .collect(Collectors.toList());
            pb.writeInt(elements.size());
            elements.forEach(element -> element.toBytes(pb));
        });
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        FriendlyByteBuf pb = new FriendlyByteBuf(buf);
        wireSystems.clear();
        int count = pb.readInt();
        for (int i = 0; i < count; i++) {
            int wiresHashCode = pb.readInt();
            int localCount = pb.readInt();

            ImmutableList.Builder<WireSystem.WireElement> elements = ImmutableList.builder();
            for (int j = 0; j < localCount; j++) {
                elements.add(new WireSystem.WireElement(pb));
            }
            WireSystem wireSystem = new WireSystem(elements.build(), null);

            wireSystems.put(wiresHashCode, wireSystem);
        }
    }

    public static final IMessageHandler<MessageWireSystems, IMessage> HANDLER = (message, ctx) ->
    {
        ClientWireSystems.INSTANCE.wireSystems.clear();
        ClientWireSystems.INSTANCE.wireSystems.putAll(message.wireSystems);
        return null;
    };
}
