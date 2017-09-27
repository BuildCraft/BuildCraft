/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.template.TemplateApi;

import buildcraft.builders.snapshot.Template.BuildingInfo;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
    public TemplateBuilder(ITileForTemplateBuilder tile) {
        super(tile);
    }

    @Nonnull
    @Override
    protected Template.BuildingInfo getBuildingInfo() {
        return tile.getTemplateBuildingInfo();
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        BuildingInfo info = getBuildingInfo();
        if (!info.box.contains(blockPos)) {
            return true;
        }
        blockPos = info.fromWorld(blockPos);
        return !info.getSnapshot().data.shouldFill(blockPos.getX(), blockPos.getY(), blockPos.getZ());
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
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) tile.getWorldBC(),
            tile.getOwner(), tile.getBuilderPos());
        ItemStack firstItem = placeTask.items.get(0);
        fakePlayer.setHeldItem(fakePlayer.getActiveHand(), firstItem);
        return TemplateApi.templateRegistry.handle(tile.getWorldBC(), placeTask.pos, fakePlayer, firstItem);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);
        tile.getInvResources().insert(placeTask.items.get(0), false, false);
    }

    @Override
    protected boolean shouldIgnore(BlockPos pos) {
        BuildingInfo info = getBuildingInfo();
        if (!info.box.contains(pos)) {
            return true;
        }
        BlockPos tplPos = info.fromWorld(pos);
        FilledTemplate template = info.getSnapshot().data;
        int x = tplPos.getX();
        int y = tplPos.getY();
        int z = tplPos.getZ();
        return template.shouldIgnore(x, y, z);
    }

    @Override
    protected boolean isBlockCorrect(BlockPos pos) {
        BuildingInfo info = getBuildingInfo();
        if (!info.box.contains(pos)) {
            return true;
        }
        BlockPos tplPos = info.fromWorld(pos);
        FilledTemplate template = info.getSnapshot().data;
        int x = tplPos.getX();
        int y = tplPos.getY();
        int z = tplPos.getZ();
        if (template.shouldIgnore(x, y, z)) {
            return true;
        }
        boolean isAir = tile.getWorldBC().isAirBlock(pos);
        return isAir != template.shouldFill(x, y, z);
    }
}
