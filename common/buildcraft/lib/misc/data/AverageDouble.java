/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.misc.data;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.core.lib.utils.MathUtils;
import buildcraft.lib.misc.NBTUtils;

public class AverageDouble implements INBTSerializable<NBTTagCompound> {
    private double[] data;
    private int pos, precise;
    private double averageRaw, tickValue;

    public AverageDouble(int precise) {
        this.precise = precise;
        this.data = new double[precise];
        this.pos = 0;
    }

    public double getAverage() {
        return averageRaw / precise;
    }

    public void tick(double value) {
        internalTick(tickValue + value);
        tickValue = 0;
    }

    public void tick() {
        internalTick(tickValue);
        tickValue = 0;
    }

    private void internalTick(double value) {
        pos = ++pos % precise;
        double oldValue = data[pos];
        data[pos] = value;
        if (pos == 0) {
            averageRaw = 0;
            for (double iValue : data) {
                averageRaw += iValue;
            }
        } else {
            averageRaw = averageRaw - oldValue + value;
        }
    }

    public void push(double value) {
        tickValue += value;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("pos", pos);
        nbt.setInteger("precise", precise);
        nbt.setDouble("averageRaw", averageRaw);
        nbt.setDouble("tickValue", tickValue);
        nbt.setTag("data", NBTUtils.writeDoubleArray(data));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        precise = MathUtils.clamp(nbt.getInteger("precise"), 1, Short.MAX_VALUE);
        pos = MathUtils.clamp(nbt.getInteger("pos"), 0, precise);
        averageRaw = nbt.getDouble("averageRaw");
        tickValue = nbt.getDouble("tickValue");
        data = NBTUtils.readDoubleArray(nbt.getTag("data"), precise);
    }
}
