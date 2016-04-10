package buildcraft.lib.mj.helpers;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.INBTSerializable;

public class MjBattery implements INBTSerializable<NBTTagCompound> {
    private final int capacity;
    private int milliJoules = 0;

    public MjBattery(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("stored", milliJoules);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        milliJoules = nbt.getInteger("stored");
    }
}
