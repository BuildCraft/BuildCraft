/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.TilesAPI;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.factory.util.IAutoCraft;
import buildcraft.factory.util.WorkbenchCrafting;

public abstract class TileAutoWorkbenchBase extends TileBC_Neptune implements ITickable, IDebuggable, IAutoCraft {
    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResult;
    public final ItemHandlerSimple invOverflow;
    private final WorkbenchCrafting crafting;
    private int progress = -1;
    public IRecipe currentRecipe;
    private List<ItemStack> requirements = null;

    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.GUI_ONLY);

    public TileAutoWorkbenchBase(int width, int height) {
        int slots = width * height;
        invBlueprint = itemManager.addInvHandler("blueprint", slots, ItemHandlerManager.EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", slots, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = itemManager.addInvHandler("result", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
        invOverflow = itemManager.addInvHandler("overflow", slots, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(width, height, invBlueprint);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> progress >= 0, EnumPipePart.VALUES);
    }

    @Override
    public void update() {
        deltaManager.tick();
        updateRecipe();
        if (getWorld().isRemote) {
            return;
        }
        if (canWork()) {
            if (progress == 0) {
                deltaProgress.addDelta(0, 200, 1);
                deltaProgress.addDelta(200, 205, -1);
            }
            if (progress < 200) {
                progress++;
                return;
            }
            if (invOverflow.getStackInSlot(1).isEmpty()) {
                craft();
                progress = 0;
            }
        } else if (progress != -1) {
            progress = -1;
            deltaProgress.setValue(0);
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add(currentRecipe == null ? "No recipe set" : currentRecipe.toString());
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
        return invResult;
    }

    @Override
    public WorkbenchCrafting getWorkbenchCrafting() {
        return crafting;
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

    @Override
    public World getWorldForAutoCrafting() {
        return getWorld();
    }

    @Override
    public BlockPos getPosForAutoCrafting() {
        return getPos();
    }
}
