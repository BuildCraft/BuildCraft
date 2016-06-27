package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IBptWriter {
    /** Gets the name to identify this when reading and writing to disk. */
    ResourceLocation getRegistryName();

    NBTTagCompound serializeNBT();
}
