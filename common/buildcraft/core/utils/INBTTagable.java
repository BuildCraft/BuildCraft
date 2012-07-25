package buildcraft.core.utils;

import net.minecraft.src.NBTTagCompound;

public interface INBTTagable {

	void readFromNBT(NBTTagCompound nbttagcompound);

	void writeToNBT(NBTTagCompound nbttagcompound);
}
