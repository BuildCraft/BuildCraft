/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import ct.buildcraft.api.core.BuildCraftAPI;
import ct.buildcraft.api.recipes.IngredientStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @deprecated TEMPORARY CLASS DO NOT USE!
 */
@Deprecated
public class $AssemblyRecipeBasic extends $AssemblyRecipe {
    private final long requiredMicroJoules;
    private final ImmutableSet<IngredientStack> requiredStacks;
    private final ImmutableSet<ItemStack> output;

    public $AssemblyRecipeBasic(ResourceLocation name, long requiredMicroJoules, ImmutableSet<IngredientStack> requiredStacks, @Nonnull ItemStack output) {
        this.requiredMicroJoules = requiredMicroJoules;
        this.requiredStacks = ImmutableSet.copyOf(requiredStacks);
        this.output = ImmutableSet.of(output);
        setRegistryName(name);
    }

    public $AssemblyRecipeBasic(String name, long requiredMicroJoules, ImmutableSet<IngredientStack> requiredStacks, @Nonnull ItemStack output) {
        this(new ResourceLocation(name)/*BuildCraftAPI.nameToResourceLocation(name)*/, requiredMicroJoules, requiredStacks, output);
    }

    public $AssemblyRecipeBasic(String name, long requiredMicroJoules, Set<IngredientStack> requiredStacks, @Nonnull ItemStack output) {
        this(name, requiredMicroJoules, ImmutableSet.copyOf(requiredStacks), output);
    }

    @Override
    public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
        if (requiredStacks.stream().allMatch((definition) -> inputs.stream().anyMatch((stack) -> !stack.isEmpty() && definition.ingredient.test(stack) && stack.getCount() >= definition.count)))
            return output;
        return Collections.emptySet();
    }

    @Override
    public Set<ItemStack> getOutputPreviews() {
        return output;
    }

    @Override
    public Set<IngredientStack> getInputsFor(@Nonnull ItemStack output) {
        return requiredStacks;
    }

    @Override
    public long getRequiredMicroJoulesFor(@Nonnull ItemStack output) {
        return requiredMicroJoules;
    }
}
