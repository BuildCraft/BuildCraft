/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.template.TemplateApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Collections;
import java.util.List;

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
        return !getBuildingInfo().box.contains(blockPos) ||
                !getBuildingInfo().getSnapshot().data.get(
                        getBuildingInfo().getSnapshot().posToIndex(
                                getBuildingInfo().fromWorld(blockPos)
                        )
                );
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return tile.getWorldBC().isEmptyBlock(blockPos);
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
                (ServerLevel) tile.getWorldBC(),
                tile.getOwner(),
                tile.getBuilderPos()
        );
//        fakePlayer.setHeldItem(fakePlayer.getActiveHand(), placeTask.items.get(0));
        fakePlayer.setItemInHand(fakePlayer.getUsedItemHand(), placeTask.items.get(0));
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
        return !isAir(blockPos) && !tile.getWorldBC().isEmptyBlock(blockPos);
    }
}
