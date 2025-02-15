/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public final class CraftingUtil {

    /** Deactivate constructor */
    private CraftingUtil() {
    }

    // public static Recipe findMatchingRecipe(CraftingContainer par1InventoryCrafting, Level par2World)
    public static CraftingRecipe findMatchingRecipe(CraftingContainer par1InventoryCrafting, Level par2World) {
//        List<IRecipe> recipes = GameRegistry.findRegistry(IRecipe.class).getValues();
        List<CraftingRecipe> recipes = par2World.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, par1InventoryCrafting, par2World);
//        for (IRecipe recipe : recipes)
        for (CraftingRecipe recipe : recipes) {
            if (recipe.matches(par1InventoryCrafting, par2World)) {
                return recipe;
            }
        }
        return null;
    }
}
