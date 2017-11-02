/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.template.TemplateApi;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
    public TemplateBuilder(ITileForTemplateBuilder tile) {
        super(tile);
    }

    @Override
    protected Template.BuildingInfo getBuildingInfo() {
        return tile.getTemplateBuildingInfo();
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        if (!getBuildingInfo().box.contains(blockPos)) {
            return true;
        }
        BlockPos pos = getBuildingInfo().fromWorld(blockPos);
        return !getBuildingInfo().getSnapshot().data.shouldFill(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return tile.getWorldBC().isAirBlock(blockPos);
    }

    @Override
    protected boolean isReadyToPlace(BlockPos blockPos) {
        return true;
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return !tile.getInvResources().extract(null, 1, 1, true).isEmpty();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Collections.singletonList(tile.getInvResources().extract(null, 1, 1, false));
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(
            (WorldServer) tile.getWorldBC(),
            tile.getOwner(),
            tile.getBuilderPos()
        );
        fakePlayer.setHeldItem(fakePlayer.getActiveHand(), placeTask.items.get(0));
        return TemplateApi.templateRegistry.handle(
            tile.getWorldBC(),
            placeTask.pos,
            fakePlayer,
            placeTask.items.get(0)
        );
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);
        tile.getInvResources().insert(placeTask.items.get(0), false, false);
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return  !isAir(blockPos) && !tile.getWorldBC().isAirBlock(blockPos);
    }
}
