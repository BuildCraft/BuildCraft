/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.delta;

import buildcraft.lib.net.IPayloadWriter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DeltaManager {
    public enum EnumDeltaMessage {
        ADD_SINGLE,
        SET_VALUE,
        CURRENT_STATE
    }

    public enum EnumNetworkVisibility {
        NONE,
        GUI_ONLY,
        RENDER,
    }

    public interface IDeltaMessageSender {
        /** @param type The type of message. NEVER {@link EnumDeltaMessage#CURRENT_STATE}. */
        void sendDeltaMessage(boolean gui, EnumDeltaMessage type, IPayloadWriter writer);
    }

    private final IDeltaMessageSender sender;
    private final Map<EnumNetworkVisibility, List<DeltaInt>> deltas = new EnumMap<>(EnumNetworkVisibility.class);

    public DeltaManager(IDeltaMessageSender sender) {
        this.sender = sender;
        deltas.put(EnumNetworkVisibility.NONE, new ArrayList<>());
        deltas.put(EnumNetworkVisibility.GUI_ONLY, new ArrayList<>());
        deltas.put(EnumNetworkVisibility.RENDER, new ArrayList<>());
    }

    public DeltaInt addDelta(String name, EnumNetworkVisibility visibility) {
        DeltaInt delta = new DeltaInt(name, visibility, this);
        deltas.get(visibility).add(delta);
        return delta;
    }

    public void tick() {
        for (List<DeltaInt> innerList : deltas.values()) {
            for (DeltaInt delta : innerList) {
                delta.tick();
            }
        }
    }

    public void receiveDeltaData(boolean gui, EnumDeltaMessage type, PacketBuffer buffer) {
        EnumNetworkVisibility visibility = gui ? EnumNetworkVisibility.GUI_ONLY : EnumNetworkVisibility.RENDER;
        if (type == EnumDeltaMessage.CURRENT_STATE) {
            for (DeltaInt delta : deltas.get(visibility)) {
                delta.receiveData(EnumDeltaMessage.CURRENT_STATE, buffer);
            }
        } else {
            int index = buffer.readUnsignedByte();
            DeltaInt delta = deltas.get(visibility).get(index);
            delta.receiveData(type, buffer);
        }
    }

    void sendDeltaMessage(EnumDeltaMessage type, DeltaInt from, IPayloadWriter writer) {
        EnumNetworkVisibility visibility = from.visibility;
        if (visibility == EnumNetworkVisibility.NONE) return;
        boolean gui = visibility == EnumNetworkVisibility.GUI_ONLY;

        final int index = deltas.get(from.visibility).indexOf(from);
        if (index == -1) throw new IllegalArgumentException("Unknown delta!");

        sender.sendDeltaMessage(gui, type, (buffer) ->
        {
            buffer.writeByte(index);
            writer.write(buffer);
        });
    }

    public void writeDeltaState(boolean gui, PacketBuffer buffer) {
        EnumNetworkVisibility visibility = gui ? EnumNetworkVisibility.GUI_ONLY : EnumNetworkVisibility.RENDER;
        for (DeltaInt delta : deltas.get(visibility)) {
            delta.writeState(buffer);
        }
    }

    public void readFromNBT(CompoundNBT nbt) {
        for (List<DeltaInt> innerList : deltas.values()) {
            for (DeltaInt delta : innerList) {
                delta.readFromNBT(nbt.getCompound(delta.name));
            }
        }
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (List<DeltaInt> innerList : deltas.values()) {
            for (DeltaInt delta : innerList) {
                nbt.put(delta.name, delta.writeToNBT());
            }
        }
        return nbt;
    }
}
