package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

public interface SchematicFactoryNBTBlock {
    SchematicBlock createFromNBT(NBTTagCompound nbt) throws SchematicException;
}
