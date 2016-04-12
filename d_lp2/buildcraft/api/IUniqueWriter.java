package buildcraft.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IUniqueWriter {
    /** Gets the name to identify this when reading and writing to disk. */
    ResourceLocation getRegistryName();

    NBTTagCompound serializeNBT();
}
