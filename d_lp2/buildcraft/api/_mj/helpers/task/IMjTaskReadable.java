package buildcraft.api._mj.helpers.task;

import net.minecraft.nbt.NBTTagCompound;

public interface IMjTaskReadable extends IMjTask {
    String getUID();

    void deserializeNBT(NBTTagCompound nbt);
}
