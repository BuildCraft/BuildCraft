package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

/** Not quite as helpful because of extra args?
 * 
 * @param <B> */
public interface IBptReader<B extends IBptWriter> {
    B deserialize(NBTTagCompound nbt, IBuilderAccessor accessor);
}
