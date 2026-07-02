/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 */
package ct.buildcraft.api.robots;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import ct.buildcraft.api.core.BlockIndex;

public class ResourceIdBlock extends ResourceId {
    public BlockIndex index = new BlockIndex();
    @Nullable
    public Direction side;

    public ResourceIdBlock() {
    }

    public ResourceIdBlock(int x, int y, int z) {
        index = new BlockIndex(x, y, z);
    }

    public ResourceIdBlock(BlockPos pos) {
        index = new BlockIndex(pos);
    }

    public ResourceIdBlock(BlockIndex index) {
        this.index = index;
    }

    public ResourceIdBlock(BlockEntity blockEntity) {
        index = new BlockIndex(blockEntity);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        ResourceIdBlock other = (ResourceIdBlock) obj;
        return index.equals(other.index) && side == other.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, side);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        CompoundTag indexTag = new CompoundTag();
        index.writeTo(indexTag);
        nbt.put("index", indexTag);
        nbt.putByte("side", (byte) (side == null ? -1 : side.ordinal()));
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        index = new BlockIndex(nbt.getCompound("index"));
        byte sideId = nbt.getByte("side");
        side = sideId >= 0 && sideId < Direction.values().length ? Direction.values()[sideId] : null;
    }
}
