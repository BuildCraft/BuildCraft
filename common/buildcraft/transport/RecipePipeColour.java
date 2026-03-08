/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

//TODO: convert to factory if needed, currently not used
public class RecipePipeColour implements IRecipe<IInventory>, IRecipeViewable {

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
    public boolean matches(IInventory inv, World worldIn) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public ItemStack assemble(IInventory inv) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(IInventory inv) {
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

    @Nullable
    @Override
    public ResourceLocation getId() {
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public IRecipeType<RecipePipeColour> getType() {
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        throw new AbstractMethodError("Implement this!");
    }
}
