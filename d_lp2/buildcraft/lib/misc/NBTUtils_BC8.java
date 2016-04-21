package buildcraft.lib.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

/** Helper methods for reading and writing various objects to and from NBT. */
public class NBTUtils_BC8 {
    public static NBTBase writeDoubleArray(double[] data) {
        NBTTagList list = new NBTTagList();
        for (double d : data) {
            list.appendTag(new NBTTagDouble(d));
        }
        return list;
    }

    public static double[] readDoubleArray(NBTBase tag, int intendedLength) {
        double[] arr = new double[intendedLength];
        if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            for (int i = 0; i < list.tagCount() && i < intendedLength; i++) {
                arr[i] = list.getDoubleAt(i);
            }
        }
        return arr;
    }
}
