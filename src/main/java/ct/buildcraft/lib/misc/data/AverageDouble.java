/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc.data;

import ct.buildcraft.lib.misc.NBTUtilBC;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;

public class AverageDouble implements INBTSerializable<CompoundTag> {
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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("pos", pos);
        nbt.putInt("precise", precise);
        nbt.putDouble("averageRaw", averageRaw);
        nbt.putDouble("tickValue", tickValue);
        nbt.put("data", NBTUtilBC.writeDoubleArray(data));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        precise = Mth.clamp(nbt.getInt("precise"), 1, Short.MAX_VALUE);
        pos = Mth.clamp(nbt.getInt("pos"), 0, precise);
        averageRaw = nbt.getDouble("averageRaw");
        tickValue = nbt.getDouble("tickValue");
        data = NBTUtilBC.readDoubleArray(nbt.get("data"), precise);
    }
}
