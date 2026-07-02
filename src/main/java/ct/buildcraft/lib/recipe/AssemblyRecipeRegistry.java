/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class AssemblyRecipeRegistry  {
    public static final Map<ResourceLocation, AssemblyRecipe> REGISTRY = new HashMap<>();

    public static void register(AssemblyRecipe recipe) {
        REGISTRY.put(recipe.getId(), recipe);
    }


    @Nonnull
    public static List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn) {
        List<AssemblyRecipe> all = new ArrayList<>();
        for (AssemblyRecipe ar : REGISTRY.values()) {
/*            if (!ar.getResultItem(possibleIn).isEmpty()) {
                all.add(ar);
            }*/
        }
        BCLog.d("AssemblyRecipeRegistry:Called unimplemented method");
        return all;
    }
}
