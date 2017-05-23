/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageWireSystems implements IMessage {
    private Map<Integer, WireSystem> wireSystems = new HashMap<>();

    public MessageWireSystems() {
    }

    public MessageWireSystems(Map<Integer, WireSystem> wireSystems) {
        this.wireSystems = wireSystems;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(wireSystems.size());
        wireSystems.forEach((wiresHashCode, wireSystem) -> {
            buf.writeInt(wiresHashCode);
            List<WireSystem.WireElement> elements = wireSystem.elements.stream()
                    .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
                    .collect(Collectors.toList());
            buf.writeInt(elements.size());
            elements.forEach(element -> element.toBytes(buf));
        });
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        wireSystems.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            WireSystem wireSystem = new WireSystem();
            int wiresHashCode = buf.readInt();
            int localCount = buf.readInt();
            for (int j = 0; j < localCount; j++) {
                wireSystem.elements.add(new WireSystem.WireElement(buf));
            }
            wireSystems.put(wiresHashCode, wireSystem);
        }
    }

    public static final IMessageHandler<MessageWireSystems, IMessage> HANDLER = (message, ctx) -> {
        ClientWireSystems.INSTANCE.wireSystems.clear();
        ClientWireSystems.INSTANCE.wireSystems.putAll(message.wireSystems);
        return null;
    };
}
