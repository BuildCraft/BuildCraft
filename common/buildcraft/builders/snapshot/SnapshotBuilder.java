/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): SnapshotBuilder logic deferred — BlockUtil.breakBlockAndGetDrops + IItemTransactor migration pending
package buildcraft.builders.snapshot;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.INBTSerializable;

public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> implements INBTSerializable<NbtCompound> {

    protected final T tile;

    public SnapshotBuilder(T tile) {
        this.tile = tile;
    }

    protected abstract Snapshot.BuildingInfo getBuildingInfo();

    protected abstract boolean isAir(BlockPos blockPos);

    protected abstract boolean canPlace(BlockPos blockPos);

    protected abstract boolean isReadyToPlace(BlockPos blockPos);

    protected abstract boolean hasEnoughToPlaceItems(BlockPos blockPos);

    protected abstract List<ItemStack> getToPlaceItems(BlockPos blockPos);

    protected abstract boolean doPlaceTask(PlaceTask placeTask);

    protected abstract boolean isBlockCorrect(BlockPos blockPos);

    public void tick() {
        // STUB
    }

    public boolean isBuildFinished() {
        return false;
    }

    public void updateSnapshot() {
        // STUB
    }

    @Override
    public NbtCompound serializeNBT() {
        return new NbtCompound();
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) {
        // STUB
    }

    public static class PlaceTask {
        public final BlockPos pos;
        public final List<ItemStack> required;
        public PlaceTask(BlockPos pos, List<ItemStack> required) {
            this.pos = pos;
            this.required = required;
        }
    }
}
