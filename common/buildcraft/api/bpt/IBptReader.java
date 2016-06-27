package buildcraft.api.bpt;

import net.minecraft.nbt.NBTTagCompound;

/** Not quite as helpful because of extra args?
 * 
 * @date Created on 12 Apr 2016 by AlexIIL
 *
 * @param <B> */
public interface IBptReader<B extends IBptWriter> {
    B deserialize(NBTTagCompound nbt, IBuilderAccessor accessor);
}
