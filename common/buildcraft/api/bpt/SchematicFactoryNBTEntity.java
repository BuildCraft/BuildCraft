package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

public interface SchematicFactoryNBTEntity {
    SchematicEntity<?> createFromNBT(NBTTagCompound nbt) throws SchematicException;
}
