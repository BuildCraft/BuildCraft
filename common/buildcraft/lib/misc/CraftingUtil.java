/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;

public final class CraftingUtil {

    /**
     * Deactivate constructor
     */
    private CraftingUtil() {
    }

    public static IRecipe findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World) {
            List<IRecipe> recipes = GameRegistry.findRegistry(IRecipe.class).getValues();
            for (IRecipe recipe : recipes) {
                if (recipe.matches(par1InventoryCrafting, par2World)) {
                    return recipe;
                }
            }
            return null;

    }
}
