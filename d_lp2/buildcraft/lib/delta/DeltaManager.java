package buildcraft.lib.delta;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.PacketBuffer;

import buildcraft.lib.net.command.IPayloadWriter;

public class DeltaManager {
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

    public void tick(long now) {
        for (DeltaInt delta : deltas) {
            delta.tick(now);
        }
    }

    public void receiveDeltaData(PacketBuffer buffer) {
        int index = buffer.readUnsignedByte();
        DeltaInt delta = deltas.get(index);
        delta.receiveData(buffer);
    }

    void sendDeltaMessage(DeltaInt from, IPayloadWriter writer) {
        final int index = deltas.indexOf(from);
        if (index == -1) throw new IllegalArgumentException("Unknown delta!");
        sender.sendDeltaMessage(buffer -> {
            buffer.writeByte(index);
            writer.write(buffer);
        });
    }

    public interface IDeltaMessageSender {
        void sendDeltaMessage(IPayloadWriter writer);
    }
}
