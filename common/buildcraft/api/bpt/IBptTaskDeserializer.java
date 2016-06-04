package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

/** Provides a way to deserialize actions from NBT. This is a functional interface so you should provide this with a
 * constructor to your action, if you are using Java 8. */
public interface IBptTaskDeserializer {
    IBptTask deserializeNBT(NBTTagCompound nbt);
}
