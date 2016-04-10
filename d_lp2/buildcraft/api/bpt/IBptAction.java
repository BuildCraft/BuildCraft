package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IBptAction {
    /** Gets the name to identify this when reading and writing to disk. */
    ResourceLocation getRegistryName();

    void run(IBuilder builder);

    NBTTagCompound serializeNBT();
}
