/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import buildcraft.api.recipes.AssemblyRecipe;

import buildcraft.silicon.BCSilicon;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class AssemblyRecipeRegistry  {
    public static IForgeRegistry<AssemblyRecipe> REGISTRY;

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.NewRegistry event) {
        REGISTRY = new RegistryBuilder().disableSaving().setType(AssemblyRecipe.class).setName(new ResourceLocation("buildcraftlib:AssemblyRecipeRegistry")).create();
    }

    @Nonnull
    public static List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn) {
        List<AssemblyRecipe> all = new ArrayList<>();
        for (AssemblyRecipe ar : REGISTRY.getValues()) {
            if (!ar.getOutputs(possibleIn).isEmpty()) {
                all.add(ar);
            }
        }
        return all;
    }
}
