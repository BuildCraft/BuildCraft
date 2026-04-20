/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.wire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

public class MessageWireSystems  {
    private Map<Integer, WireSystem> wireSystems = new HashMap<>();

    @SuppressWarnings("unused")
    public MessageWireSystems() {
    }

    public MessageWireSystems(Map<Integer, WireSystem> wireSystems) {
        this.wireSystems = wireSystems;
    }

    public static void toBytes(MessageWireSystems msg, FriendlyByteBuf buf) {
        FriendlyByteBuf pb = new FriendlyByteBuf(buf);
        pb.writeInt(msg.wireSystems.size());
        msg.wireSystems.forEach((wiresHashCode, wireSystem) -> {
            pb.writeInt(wiresHashCode);
            List<WireSystem.WireElement> elements = wireSystem.elements.stream()
                    .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
                    .collect(Collectors.toList());
            pb.writeInt(elements.size());
            elements.forEach(element -> element.toBytes(pb));
        });
    }

    public MessageWireSystems(FriendlyByteBuf buf) {
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

    public static final BiConsumer<MessageWireSystems, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	ctx.get().enqueueWork(() -> {  
	        ClientWireSystems.INSTANCE.wireSystems.clear();
	        ClientWireSystems.INSTANCE.wireSystems.putAll(message.wireSystems);
	        return;
	    });
    	ctx.get().setPacketHandled(true);
    };
}
