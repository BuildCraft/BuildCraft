package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface SchematicFactoryNBTBlock {
    SchematicBlock createFromNBT(NBTTagCompound nbt) throws SchematicException;
}
