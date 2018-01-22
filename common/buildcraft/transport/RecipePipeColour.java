/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

public class RecipePipeColour implements IRecipe, IRecipeViewable {

    private final ItemStack output;
    /** Single-dimension because all pipe recipes use 3 items or less. */
    private final Object[] required;
    private final boolean shaped;

    public RecipePipeColour(ItemStack out, Object[] required, boolean shaped) {
        this.output = out;
        this.required = required;
        this.shaped = shaped;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public int getRecipeSize() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public ChangingItemStack[] getRecipeInputs() {
        return null;
    }

    @Override
    public ChangingItemStack getRecipeOutputs() {
        return null;
    }
}
