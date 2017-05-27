/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.util.List;

import buildcraft.factory.tile.TileAutoWorkbenchBase;
import buildcraft.factory.util.IAutoCraft;
import buildcraft.factory.util.WorkbenchCrafting;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileAdvancedCraftingTable extends TileLaserTableBase implements IAutoCraft {
    public final ItemHandlerSimple invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, ItemHandlerManager.EnumAccess.PHANTOM);
    public final ItemHandlerSimple invMaterials = itemManager.addInvHandler("materials", 5 * 3, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invResults = itemManager.addInvHandler("result", 3 * 3, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
    private final WorkbenchCrafting crafting = new WorkbenchCrafting(3, 3, invBlueprint);
    private int progress = -1;
    public IRecipe currentRecipe;
    private List<ItemStack> requirements = null;

    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.GUI_ONLY);

    public long getTarget() {
        return 40 * MjAPI.MJ;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return;
        }

        updateRecipe();
        if (canWork()) {
            if (progress == 0) {
                deltaProgress.addDelta(0, 200, 1);
                deltaProgress.addDelta(200, 205, -1);
            }
            if (progress < 200) {
                progress++;
                return;
            }
            craft();
            progress = 0;
        } else if (progress != -1) {
            progress = -1;
            deltaProgress.setValue(0);
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public boolean hasWork() {
        return canWork();
    }

    @Override
    public ItemHandlerSimple getInvBlueprint() {
        return invBlueprint;
    }

    @Override
    public ItemHandlerSimple getInvMaterials() {
        return invMaterials;
    }

    @Override
    public ItemHandlerSimple getInvResult() {
        return invResults;
    }

    @Override
    public WorkbenchCrafting getWorkbenchCrafting() {
        return crafting;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public IRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public void setCurrentRecipe(IRecipe recipe) {
        currentRecipe = recipe;
    }

    @Override
    public void setRequirements(List<ItemStack> stacks) {
        requirements = stacks;
    }

    @Override
    public List<ItemStack> getRequirements() {
        return requirements;
    }
}
