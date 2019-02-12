/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class RecipeLookupHelper {
    public static final Map<String, IStackRecipes> handlerTypes = new HashMap<>();

    static {
        handlerTypes.put("smelting", GuideSmeltingRecipes.INSTANCE);
        handlerTypes.put("crafting", GuideCraftingRecipes.INSTANCE);
        handlerTypes.put("assembling", GuideAssemblyRecipes.INSTANCE);
    }

    public static List<GuidePartFactory> getAllUsages(@Nonnull ItemStack stack, Profiler prof) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            prof.startSection(handler.getClass().getName().replace('.', '/'));
            List<GuidePartFactory> recipes = handler.getUsages(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
            prof.endSection();
        }
        return list;
    }

    public static List<GuidePartFactory> getAllRecipes(@Nonnull ItemStack stack, Profiler prof) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : handlerTypes.values()) {
            prof.startSection(handler.getClass().getName().replace('.', '/'));
            List<GuidePartFactory> recipes = handler.getRecipes(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
            prof.endSection();
        }
        return list;
    }
}
