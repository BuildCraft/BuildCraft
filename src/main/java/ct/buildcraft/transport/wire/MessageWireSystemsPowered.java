/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.wire;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import ct.buildcraft.api.transport.IWireManager;
import ct.buildcraft.api.transport.pipe.IPipeHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class MessageWireSystemsPowered {
    private Map<Integer, Boolean> hashesPowered = new HashMap<>();

    @SuppressWarnings("unused")
    public MessageWireSystemsPowered() {
    }

    public MessageWireSystemsPowered(Map<Integer, Boolean> hashesPowered) {
        this.hashesPowered = hashesPowered;
    }

    public MessageWireSystemsPowered(FriendlyByteBuf buf) {
        hashesPowered.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            hashesPowered.put(buf.readInt(), buf.readBoolean());
        }
    }
    
    public static void toBytes(MessageWireSystemsPowered msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.hashesPowered.size());
        msg.hashesPowered.forEach((wiresHashCode, powered) -> {
            buf.writeInt(wiresHashCode);
            buf.writeBoolean(powered);
        });
    }

    public static final BiConsumer<MessageWireSystemsPowered, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	ctx.get().enqueueWork(() -> {    
    		message.hashesPowered.entrySet().stream()
	                .map(hashPowered ->
	                        Pair.of(
	                                ClientWireSystems.INSTANCE.wireSystems.get(hashPowered.getKey()),
	                                hashPowered.getValue()
	                        )
	                )
	                .flatMap(systemPowered ->
	                        systemPowered.getLeft().elements.stream()
	                                .map(element ->
	                                        Pair.of(element, systemPowered.getRight())
	                                )
	                )
	                .forEach(elementPowered -> {
	                    WireSystem.WireElement element = elementPowered.getLeft();
	                    boolean powered = elementPowered.getRight();
	                    if (element.type == WireSystem.WireElement.Type.WIRE_PART) {
	                        BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(element.blockPos);
	                        if (tile instanceof IPipeHolder) {
	                            IPipeHolder holder = (IPipeHolder) tile;
	                            IWireManager iWireManager = holder.getWireManager();
	                            if (iWireManager instanceof WireManager) {
	                                WireManager wireManager = (WireManager) iWireManager;
	                                if (wireManager.getColorOfPart(element.wirePart) != null) {
	                                    if (powered) {
	                                        wireManager.poweredClient.add(element.wirePart);
	                                    } else {
	                                        wireManager.poweredClient.remove(element.wirePart);
	                                    }
	                                }
	                            }
	                        }
	                    }
	                });
	        return;
    	});
    	ctx.get().setPacketHandled(true);
    };
}
