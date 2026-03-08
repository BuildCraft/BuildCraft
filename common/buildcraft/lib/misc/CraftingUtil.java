/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

import java.util.List;

public final class CraftingUtil {

    /** Deactivate constructor */
    private CraftingUtil() {
    }

    // public static IRecipe findMatchingRecipe(CraftingInventory par1InventoryCrafting, World par2World)
    public static ICraftingRecipe findMatchingRecipe(CraftingInventory par1InventoryCrafting, World par2World) {
//        List<IRecipe> recipes = GameRegistry.findRegistry(IRecipe.class).getValues();
        List<ICraftingRecipe> recipes = par2World.getRecipeManager().getRecipesFor(IRecipeType.CRAFTING, par1InventoryCrafting, par2World);
//        for (IRecipe recipe : recipes)
        for (ICraftingRecipe recipe : recipes) {
            if (recipe.matches(par1InventoryCrafting, par2World)) {
                return recipe;
            }
        }
        return null;
    }
}
