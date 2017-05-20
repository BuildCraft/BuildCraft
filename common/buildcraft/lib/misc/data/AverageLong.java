/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.misc.data;

import net.minecraft.nbt.NBTTagCompound;

public class AverageLong {
    private long[] data;
    private final int precise;
    private int pos;
    private long averageRaw, tickValue;

    public AverageLong(int precise) {
        this.precise = precise;
        clear();
    }

    public void clear() {
        this.data = new long[precise];
        this.pos = 0;
    }

    public double getAverage() {
        return (double) averageRaw / precise;
    }

    public long getAverageLong() {
        return averageRaw / precise;
    }

    public void tick(long value) {
        internalTick(tickValue + value);
        tickValue = 0;
    }

    public void tick() {
        internalTick(tickValue);
        tickValue = 0;
    }

    private void internalTick(long value) {
        pos = ++pos % precise;
        long oldValue = data[pos];
        data[pos] = value;
        if (pos == 0) {
            averageRaw = 0;
            for (long iValue : data) {
                averageRaw += iValue;
            }
        } else {
            averageRaw = averageRaw - oldValue + value;
        }
    }

    public void push(long value) {
        tickValue += value;
    }

    public void writeToNbt(NBTTagCompound nbt, String subTag) {
        int[] ints = new int[precise * 2];
        for (int i = 0; i < precise; i++) {
            long val = data[i];
            ints[i * 2] = (int) val;
            ints[i * 2 + 1] = (int) (val >>> 32);
        }
        nbt.setIntArray(subTag, ints);
    }

    public void readFromNbt(NBTTagCompound nbt, String subTag) {
        int[] ints = nbt.getIntArray(subTag);
        if (ints.length >= precise * 2) {
            averageRaw = 0;
            pos = 0;
            tickValue = 0;
            for (int i = 0; i < precise; i++) {
                long val;
                val = ints[i * 2];
                val |= (long) (ints[i * 2 + 1]) << 32;
                averageRaw += val;
                data[i] = val;
            }
        }
    }
}
