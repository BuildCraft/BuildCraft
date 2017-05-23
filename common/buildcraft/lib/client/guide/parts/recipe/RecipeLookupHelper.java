/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class RecipeLookupHelper {
    public static final List<IStackRecipes> allHandlers = new ArrayList<>();

    static {
        allHandlers.add(GuideSmeltingRecipes.INSTANCE);
        allHandlers.add(GuideCraftingRecipes.INSTANCE);
        allHandlers.add(GuideAssemblyRecipes.INSTANCE);
    }

    public static List<GuidePartFactory> getAllUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : allHandlers) {
            List<GuidePartFactory> recipes = handler.getUsages(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }

    public static List<GuidePartFactory> getAllRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : allHandlers) {
            List<GuidePartFactory> recipes = handler.getRecipes(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }
}
