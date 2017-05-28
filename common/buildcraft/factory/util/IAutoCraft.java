/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.util;

import buildcraft.factory.tile.TileAutoWorkbenchBase;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IAutoCraft {

    ItemHandlerSimple getInvBlueprint();

    ItemHandlerSimple getInvMaterials();

    ItemHandlerSimple getInvResult();

    WorkbenchCrafting getWorkbenchCrafting();

    int getProgress();

    IRecipe getCurrentRecipe();

    void setCurrentRecipe(IRecipe recipe);

    default void updateRecipe() {
        IRecipe old = getCurrentRecipe();
        IRecipe newRecipe = CraftingUtil.findMatchingRecipe(getWorkbenchCrafting(), getWorld());
        setCurrentRecipe(newRecipe);
        if (newRecipe == null || old != newRecipe) {
            setRequirements(null);
        }
        if (getRequirements() == null && getCurrentRecipe() != null) {
            setRequirements(StackUtil.mergeSameItems(getInvBlueprint().stacks));
        }
    }

    World getWorld();

    void setRequirements(List<ItemStack> stacks);

    List<ItemStack> getRequirements();

    default void craft() {
        ItemStack out = getCurrentRecipe().getCraftingResult(getWorkbenchCrafting());
        InventoryUtil.drop(getWorld(), getPos(), insertInInventory(getInvResult(), out));
        for (ItemStack stack: getCurrentRecipe().getRemainingItems(getWorkbenchCrafting())) {
            InventoryUtil.drop(getWorld(), getPos(), insertInInventory(getInvResult(), stack));
        }
        for (ItemStack input: getRequirements()) {
            ItemStack toExtract = input.copy();
            for (int i = 0; i < getInvMaterials().getSlots(); i++) {
                if (StackUtil.canMerge(toExtract, getInvMaterials().getStackInSlot(i))) {
                    ItemStack extracted = getInvMaterials().extractItem(i, toExtract.getCount(), false);
                    if (extracted.getCount() == toExtract.getCount()) {
                        break;
                    } else {
                        toExtract.setCount(toExtract.getCount() - extracted.getCount());
                    }
                }
            }
        }
    }

    default ItemStack insertInInventory(ItemHandlerSimple handler, ItemStack stack) {
        ItemStack leftOver = stack;
        for (int i = 0; i < handler.getSlots(); i++) {
            leftOver = handler.insertItem(i, leftOver, false);
            if (leftOver.isEmpty()) {
                break;
            }
        }
        return leftOver;
    }

    BlockPos getPos();

    default boolean hasMaterials() {
        return getCurrentRecipe() != null && getCurrentRecipe().matches(getWorkbenchCrafting(), getWorld()) && StackUtil.containsAll(getRequirements(), StackUtil.mergeSameItems(getInvMaterials().stacks));
    }

    default boolean canWork() {
        return getCurrentRecipe() != null && hasMaterials() && getInvResult().hasRoomFor(getOutput());
    }

    default ItemStack getOutput() {
        return getCurrentRecipe() == null ? ItemStack.EMPTY : getCurrentRecipe().getCraftingResult(getWorkbenchCrafting());
    }
}
