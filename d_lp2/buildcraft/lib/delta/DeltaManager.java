package buildcraft.lib.delta;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

import buildcraft.lib.net.command.IPayloadWriter;

public class DeltaManager {
    public enum EnumDeltaMessage {
        ADD_SINGLE,
        SET_VALUE,
        CURRENT_STATE;
    }

    private final IDeltaMessageSender sender;
    private final List<DeltaInt> deltas = new ArrayList<>();

    public DeltaManager(IDeltaMessageSender sender) {
        this.sender = sender;
    }

    public DeltaInt addDelta() {
        DeltaInt delta = new DeltaInt(this);
        deltas.add(delta);
        return delta;
    }

    public void tick() {
        for (DeltaInt delta : deltas) {
            delta.tick();
        }
    }

    public void receiveDeltaData(EnumDeltaMessage type, PacketBuffer buffer) {
        if (type == EnumDeltaMessage.CURRENT_STATE) {
            for (DeltaInt delta : deltas) {
                delta.receiveData(EnumDeltaMessage.CURRENT_STATE, buffer);
            }
        } else {
            int index = buffer.readUnsignedByte();
            DeltaInt delta = deltas.get(index);
            delta.receiveData(type, buffer);
        }
    }

    void sendDeltaMessage(EnumDeltaMessage type, DeltaInt from, IPayloadWriter writer) {
        final int index = deltas.indexOf(from);
        if (index == -1) throw new IllegalArgumentException("Unknown delta!");
        sender.sendDeltaMessage(type, buffer -> {
            buffer.writeByte(index);
            writer.write(buffer);
        });
    }

    public void writeDeltaState(PacketBuffer buffer) {
        for (DeltaInt delta : deltas) {
            delta.writeState(buffer);
        }
    }

    public void readFromNBT(NBTTagList list) {
        for (int i = 0; i < list.tagCount() && i < deltas.size(); i++) {
            DeltaInt delta = deltas.get(i);
            delta.readFromNBT(list.getCompoundTagAt(i));
        }
        for (int i = list.tagCount(); i < deltas.size(); i++) {
            DeltaInt delta = deltas.get(i);
            delta.readFromNBT(new NBTTagCompound());
        }
    }

    public NBTTagList writeToNBT() {
        NBTTagList list = new NBTTagList();
        for (DeltaInt delta : deltas) {
            list.appendTag(delta.writeToNBT());
        }
        return list;
    }

    public interface IDeltaMessageSender {
        void sendDeltaMessage(EnumDeltaMessage type, IPayloadWriter writer);
    }
}
