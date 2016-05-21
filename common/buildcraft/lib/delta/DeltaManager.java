package buildcraft.lib.delta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import buildcraft.lib.net.command.IPayloadWriter;

public class DeltaManager {
    public enum EnumDeltaMessage {
        ADD_SINGLE,
        SET_VALUE,
        CURRENT_STATE;
    }

    public enum EnumNetworkVisibility {
        NONE,
        GUI_ONLY,
        RENDER,
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

        sender.sendDeltaMessage(gui, type, buffer -> {
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

    public void readFromNBT(NBTTagCompound nbt) {
        for (List<DeltaInt> innerList : deltas.values()) {
            for (DeltaInt delta : innerList) {
                delta.readFromNBT(nbt.getCompoundTag(delta.name));
            }
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (List<DeltaInt> innerList : deltas.values()) {
            for (DeltaInt delta : innerList) {
                nbt.setTag(delta.name, delta.writeToNBT());
            }
        }
        return nbt;
    }

    public interface IDeltaMessageSender {
        /** @param type The type of message. NEVER {@link EnumDeltaMessage#CURRENT_STATE}. */
        void sendDeltaMessage(boolean gui, EnumDeltaMessage type, IPayloadWriter writer);
    }
}
