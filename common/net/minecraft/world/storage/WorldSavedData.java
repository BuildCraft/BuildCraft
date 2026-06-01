// STUB(R.Chen): 1.12 WorldSavedData → PersistentState in 1.20. (already migrated in most places)
package net.minecraft.world.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public abstract class WorldSavedData extends PersistentState {
    protected final String mapName;

    public WorldSavedData(String name) { this.mapName = name; }

    // 1.12 compat methods - subclasses override these
    public void readFromNBT(NbtCompound nbt) {}
    public NbtCompound writeToNBT(NbtCompound nbt) { return nbt; }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return writeToNBT(nbt);
    }

    public void setDirty(boolean dirty) {}
}
