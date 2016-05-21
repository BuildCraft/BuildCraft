package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface SchematicFactoryNBTBlock<S extends SchematicBlock> {
    S createFromNBT(NBTTagCompound nbt, BlockPos offset);
}
