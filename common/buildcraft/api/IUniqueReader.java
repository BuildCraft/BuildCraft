package buildcraft.api;

import net.minecraft.nbt.NBTTagCompound;

/** Not quite as helpful because of extra args?
 * 
 * @date Created on 12 Apr 2016 by AlexIIL
 *
 * @param <S> */
public interface IUniqueReader<S extends IUniqueWriter> {
    S deserialize(NBTTagCompound nbt);
}
