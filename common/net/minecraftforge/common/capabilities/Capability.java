// STUB(R.Chen): Forge Capability<T> — compile shim.
package net.minecraftforge.common.capabilities;

import javax.annotation.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Direction;

public class Capability<T> {

    public interface IStorage<T> {
        @Nullable NbtElement writeNBT(Capability<T> capability, T instance, Direction side);
        void readNBT(Capability<T> capability, T instance, Direction side, NbtElement nbt);
    }

    @Nullable
    public T cast(@Nullable Object object) {
        return null;
    }
}
