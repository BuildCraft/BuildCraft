/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.delta;

import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeltaInt {
    public final String name;
    public final EnumNetworkVisibility visibility;
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
    private long tick = 0;

    public DeltaInt(String name, EnumNetworkVisibility visibility, DeltaManager manager) {
        this.name = name;
        this.visibility = visibility;
        this.manager = manager;
    }

    public void tick() {
        Iterator<DeltaIntEntry> iter = changingEntries.iterator();
        while (iter.hasNext()) {
            DeltaIntEntry delta = iter.next();
            if (tick >= delta.endTick) {
                iter.remove();
                staticEndValue += delta.delta;
            } else if (tick >= delta.startTick && !delta.hasStarted) {
                staticStartValue += delta.delta;
                delta.hasStarted = true;
            }
        }
        double dynamic = staticEndValue;
        for (DeltaIntEntry entry : changingEntries) {
            if (tick < entry.startTick) continue;
            long duration = entry.endTick - entry.startTick;
            long elapsed = tick - entry.startTick;
            double interp = elapsed / (double) duration;
            dynamic += entry.delta * interp;
        }

        dynamicValueLast = dynamicValueThis;
        dynamicValueThis = dynamic;
        tick++;
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

    void receiveData(EnumDeltaMessage type, PacketBuffer buffer) {
        if (type == EnumDeltaMessage.ADD_SINGLE) {
            long start = buffer.readLong();
            long end = buffer.readLong();
            int delta = buffer.readInt();
            DeltaIntEntry entry = new DeltaIntEntry(start + tick, end + tick, delta);
            changingEntries.add(entry);
        } else if (type == EnumDeltaMessage.SET_VALUE) {
            changingEntries.clear();
            int value = buffer.readInt();
            staticStartValue = value;
            staticEndValue = value;
            dynamicValueLast = value;
            dynamicValueThis = value;
        } else if (type == EnumDeltaMessage.CURRENT_STATE) {
            staticStartValue = buffer.readInt();
            staticEndValue = buffer.readInt();
            changingEntries.clear();
            int count = buffer.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                long start = buffer.readLong() + tick;
                long end = buffer.readLong() + tick;
                int delta = buffer.readInt();
                DeltaIntEntry entry = new DeltaIntEntry(start, end, delta);
                entry.hasStarted = buffer.readBoolean();
                changingEntries.add(entry);
            }
        }
    }

    void writeState(PacketBuffer buffer) {
        buffer.writeInt(staticStartValue);
        buffer.writeInt(staticEndValue);
        buffer.writeShort(changingEntries.size());
        for (DeltaIntEntry delta : changingEntries) {
            buffer.writeLong(delta.startTick - tick);
            buffer.writeLong(delta.endTick - tick);
            buffer.writeInt(delta.delta);
            buffer.writeBoolean(delta.hasStarted);
        }
    }

    /** Adds a delta value
     *
     * @param start
     * @param end
     * @param delta */
    public void addDelta(long start, long end, int delta) {
        DeltaIntEntry entry = new DeltaIntEntry(start + tick, end + tick, delta);
        changingEntries.add(entry);
        manager.sendDeltaMessage(EnumDeltaMessage.ADD_SINGLE, this, (buffer) ->
        {
            buffer.writeLong(entry.startTick - tick);
            buffer.writeLong(entry.endTick - tick);
            buffer.writeInt(entry.delta);
        });
    }

    /** Forgets all existing deltas and sets the values to the new value.
     *
     * @param value */
    public void setValue(int value) {
        changingEntries.clear();
        staticStartValue = value;
        staticEndValue = value;
        dynamicValueLast = value;
        dynamicValueThis = value;
        manager.sendDeltaMessage(EnumDeltaMessage.SET_VALUE, this, (buffer) -> buffer.writeInt(value));
    }

    public void readFromNBT(CompoundNBT nbt) {
        tick = nbt.getLong("tick");
        staticStartValue = nbt.getInt("static-start");
        staticEndValue = nbt.getInt("static-end");
        // dynamic is calculated every tick so there is no need to read + write it
        changingEntries.clear();
        ListNBT list = nbt.getList("changing", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entryNbt = list.getCompound(i);
            long start = entryNbt.getLong("start");
            long end = entryNbt.getLong("end");
            int delta = entryNbt.getInt("delta");
            DeltaIntEntry entry = new DeltaIntEntry(start, end, delta);
            entry.hasStarted = entryNbt.getBoolean("started");
            changingEntries.add(entry);
        }
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("tick", tick);
        nbt.putInt("static-start", staticStartValue);
        nbt.putInt("static-end", staticEndValue);
        // dynamic is calculated every tick so there is no need to read + write it
        ListNBT list = new ListNBT();
        for (DeltaIntEntry entry : changingEntries) {
            CompoundNBT entryNbt = new CompoundNBT();
            entryNbt.putLong("start", entry.startTick);
            entryNbt.putLong("end", entry.endTick);
            entryNbt.putInt("delta", entry.delta);
            entryNbt.putBoolean("started", entry.hasStarted);
            list.add(entryNbt);
        }
        nbt.put("changing", list);
        return nbt;
    }

    private static class DeltaIntEntry {
        private boolean hasStarted = false;
        private final long startTick, endTick;
        private final int delta;

        public DeltaIntEntry(long startTick, long endTick, int delta) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.delta = delta;
        }
    }
}
