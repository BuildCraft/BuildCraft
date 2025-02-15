/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

//TODO: convert to factory if needed, currently not used
public class RecipePipeColour implements Recipe<Container>, IRecipeViewable {

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
    public boolean matches(Container inv, Level worldIn) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Container inv) {
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
    public RecipeType<RecipePipeColour> getType() {
        throw new AbstractMethodError("Implement this!");
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new AbstractMethodError("Implement this!");
    }
}
