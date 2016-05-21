package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

public interface SchematicFactoryNBTEntity<S extends SchematicEntity> {
    S createFromNBT(NBTTagCompound nbt);
}
