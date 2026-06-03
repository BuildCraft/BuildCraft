/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): BlueprintBuilder logic deferred — depends on SnapshotBuilder
package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    @Override
    protected Snapshot.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        return false;
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return false;
    }

    @Override
    protected boolean isReadyToPlace(BlockPos blockPos) {
        return false;
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return false;
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return new ArrayList<>();
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        return false;
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return false;
    }
}
