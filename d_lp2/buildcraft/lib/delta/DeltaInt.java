package buildcraft.lib.delta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.network.PacketBuffer;

public class DeltaInt {
    /** The static value at the START of each delta If you add a delta entry then this value will change before delta
     * has completed. */
    private int staticStartValue = 0;
    /** The static value at the END of each delta. If you add a delta entry then this value will change after the delta
     * has completed. */
    private int staticEndValue = 0;
    /** The dynamic value, calculated each tick from all the changing deltas. */
    private double dynamicValueLast = 0, dynamicValueThis;
    public final List<DeltaIntEntry> changingEntries = new ArrayList<>();
    private final DeltaManager manager;
    private long lastTick = -1;

    public DeltaInt(DeltaManager manager) {
        this.manager = manager;
    }

    public void tick(long now) {
        Iterator<DeltaIntEntry> iter = changingEntries.iterator();
        while (iter.hasNext()) {
            DeltaIntEntry delta = iter.next();
            if (now >= delta.endTick) {
                iter.remove();
                staticEndValue += delta.delta;
            } else if (now == delta.startTick) {
                staticStartValue += delta.delta;
            }
        }
        double dynamic = staticEndValue;
        for (DeltaIntEntry entry : changingEntries) {
            if (now < entry.startTick) continue;
            long duration = entry.endTick - entry.startTick;
            long elapsed = now - entry.startTick;
            double interp = elapsed / (double) duration;
            dynamic += entry.delta * interp;
        }

        dynamicValueLast = dynamicValueThis;
        dynamicValueThis = dynamic;
        lastTick = now;
    }

    public double getDynamic(float partialTicks) {
        if (partialTicks <= 0) {
            return dynamicValueLast;
        } else if (partialTicks >= 1) {
            return dynamicValueThis;
        } else {
            double a = dynamicValueLast * (1 - partialTicks);
            double b = dynamicValueThis * partialTicks;
            return a + b;
        }
    }

    public int getStatic(boolean start) {
        return start ? staticStartValue : staticEndValue;
    }

    public void receiveData(PacketBuffer buffer) {
        long start = buffer.readLong();
        long end = buffer.readLong();
        int delta = buffer.readInt();
        DeltaIntEntry entry = new DeltaIntEntry(start, end, delta);
        if (lastTick >= start) {
            staticStartValue += delta;
        }
        changingEntries.add(entry);
    }

    public void addDelta(long start, long end, int delta) {
        DeltaIntEntry entry = new DeltaIntEntry(start, end, delta);
        if (lastTick >= start) {
            staticStartValue += delta;
        }
        changingEntries.add(entry);
        manager.sendDeltaMessage(this, (buffer) -> {
            buffer.writeLong(entry.startTick);
            buffer.writeLong(entry.endTick);
            buffer.writeInt(entry.delta);
        });
    }

    private static class DeltaIntEntry {
        private final long startTick, endTick;
        private final int delta;

        public DeltaIntEntry(long startTick, long endTick, int delta) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.delta = delta;
        }
    }
}
