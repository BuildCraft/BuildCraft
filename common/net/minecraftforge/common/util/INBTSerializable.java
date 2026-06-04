// STUB(R.Chen): Forge INBTSerializable — compile shim.
package net.minecraftforge.common.util;

import net.minecraft.nbt.NbtElement;

public interface INBTSerializable<T extends NbtElement> {
    T serializeNBT();
    void deserializeNBT(T nbt);
}
