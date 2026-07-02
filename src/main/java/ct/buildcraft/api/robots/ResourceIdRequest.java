/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 */
package ct.buildcraft.api.robots;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import ct.buildcraft.api.core.BlockIndex;

public class ResourceIdRequest extends ResourceId {
    private BlockIndex index = new BlockIndex();
    @Nullable
    private Direction side;
    private int slot;

    public ResourceIdRequest() {
    }

    public ResourceIdRequest(DockingStation station, int slot) {
        index = station.index();
        side = station.side();
        this.slot = slot;
    }

    public BlockIndex index() {
        return index;
    }

    @Nullable
    public Direction side() {
        return side;
    }

    public int slot() {
        return slot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        ResourceIdRequest other = (ResourceIdRequest) obj;
        return Objects.equals(index, other.index) && side == other.side && slot == other.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, side, slot);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        CompoundTag indexTag = new CompoundTag();
        index.writeTo(indexTag);
        nbt.put("index", indexTag);
        nbt.putByte("side", (byte) (side == null ? -1 : side.ordinal()));
        nbt.putInt("localId", slot);
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        index = new BlockIndex(nbt.getCompound("index"));
        byte sideId = nbt.getByte("side");
        side = sideId >= 0 && sideId < Direction.values().length ? Direction.values()[sideId] : null;
        slot = nbt.getInt("localId");
    }
}
