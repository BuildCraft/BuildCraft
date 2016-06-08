package buildcraft.lib.nbt;

import net.minecraft.nbt.NBTTagCompound;

/**
 * 
 */
public class NbtSquisher {
    /* Defines a compression program that can turn large, mostly-similar, dense, NBTTagCompounds into much smaller
     * variants.
     * 
     * Compression has the following steps:
     * 
     * - 1: */

    public byte[] squish(NBTTagCompound nbt) {
        NBTSquishMap map = new NBTSquishMap();
        map.addTag(nbt);

        return null;
    }

    public NBTTagCompound expand(byte[] bytes) {
        NBTSquishMap map = new NBTSquishMap();

        return null;
    }
}
