/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;

public final class CraftingUtil {

    private CraftingUtil() {}

    @Nullable
    public static CraftingRecipe findMatchingRecipe(RecipeInputInventory inv, World world) {
        // TODO(R.Chen): verify recipe lookup via RecipeManager
        if (world == null || world.getRecipeManager() == null) return null;
        return world.getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, inv, world)
            .map(match -> match.value())
            .orElse(null);
    }
}
