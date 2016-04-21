package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

public interface SchematicFactoryNBT<S extends Schematic> {
    S createFromNBT(NBTTagCompound nbt);
}
