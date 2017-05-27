/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TileAutoWorkbenchBase extends TileBC_Neptune implements ITickable, IDebuggable {
    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResult;
    public final ItemHandlerSimple invOverflow;
    private final WorkbenchCrafting crafting;
    private int progress = -1;
    public IRecipe currentRecipe;
    private List<ItemStack> requirements = null;

    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.NONE);

    public TileAutoWorkbenchBase(int width, int height) {
        int slots = width * height;
        invBlueprint = itemManager.addInvHandler("blueprint", slots, ItemHandlerManager.EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", slots, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
        invResult = itemManager.addInvHandler("result", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
        invOverflow = itemManager.addInvHandler("overflow", slots, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(width, height);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, this::hasWork, EnumPipePart.VALUES);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {

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

    private void updateRecipe() {
        IRecipe old = currentRecipe;
        this.currentRecipe = CraftingUtil.findMatchingRecipe(crafting, getWorld());
        if (currentRecipe == null || old != this.currentRecipe) {
            requirements = null;
        }
        if (requirements == null && currentRecipe != null) {
            requirements = StackUtil.mergeSameItems(invBlueprint.stacks);
        }
    }

    private void craft() {
        ItemStack out = currentRecipe.getCraftingResult(crafting);
        ItemStack leftOver = invResult.insertItem(0, out, false);
        InventoryUtil.drop(getWorld(), getPos(), leftOver);
        for (ItemStack input: requirements) {
            ItemStack toExtract = input.copy();
            for (int i = 0; i < invMaterials.getSlots(); i++) {
                if (StackUtil.canMerge(toExtract, invMaterials.getStackInSlot(i))) {
                    ItemStack extracted = invMaterials.extractItem(i, toExtract.getCount(), false);
                    if (extracted.getCount() == toExtract.getCount()) {
                        break;
                    } else {
                        toExtract.setCount(toExtract.getCount() - extracted.getCount());
                    }
                }
            }
        }
    }

    private boolean hasMaterials() {
        return currentRecipe.matches(crafting, getWorld()) && StackUtil.containsAll(requirements, StackUtil.mergeSameItems(invMaterials.stacks));
    }

    public boolean hasWork() {
        return progress >= 0;
    }

    protected boolean canWork() {
        return currentRecipe != null && hasMaterials() && StackUtil.canMerge(currentRecipe.getCraftingResult(crafting), invResult.getStackInSlot(0));
    }

    public ItemStack getOutput() {
        return currentRecipe.getCraftingResult(crafting);
    }

    protected class WorkbenchCrafting extends InventoryCrafting{

        public WorkbenchCrafting(int width, int height) {
            super(null, width, height);
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return invBlueprint.getStackInSlot(index);
        }
    }
}
