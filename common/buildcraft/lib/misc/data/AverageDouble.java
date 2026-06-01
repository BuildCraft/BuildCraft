/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import net.minecraft.nbt.NbtCompound;

import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.NBTUtilBC;

public class AverageDouble implements INBTSerializable<NbtCompound> {
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
        pos = (pos + 1) % precise;
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

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public NbtCompound createNbt() { return serializeNBT(); }
    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("pos", pos);
        nbt.putInt("precise", precise);
        nbt.putDouble("averageRaw", averageRaw);
        nbt.putDouble("tickValue", tickValue);
        nbt.put("data", NBTUtilBC.writeDoubleArray(data));
        return nbt;
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) {
        precise = MathUtil.clamp(nbt.getInt("precise"), 1, Short.MAX_VALUE);
        pos = MathUtil.clamp(nbt.getInt("pos"), 0, precise);
        averageRaw = nbt.getDouble("averageRaw");
        tickValue = nbt.getDouble("tickValue");
        data = NBTUtilBC.readDoubleArray(nbt.get("data"), precise);
    }
}
