/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.recipe;

import java.util.*;

import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.transport.BCTransport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import buildcraft.lib.recipe.IRecipeViewable;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.FacadeStateManager.FacadeBlockStateInfo;
import buildcraft.transport.plug.FacadeStateManager.FullFacadeInstance;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum FacadeAssemblyRecipes implements IAssemblyRecipeProvider, IRecipeViewable.IRecipePowered {
    INSTANCE;

    private static final int TIME_GAP = 500;
    private static final long MJ_COST = 64 * MjAPI.MJ;
    private static final ChangingObject<Long> MJ_COSTS = new ChangingObject<>(new Long[] { MJ_COST });

    @Nonnull
    @Override
    public List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possible) {
        // Require 3 structure pipes -- check for those first as its much cheaper
        if (!StackUtil.contains(new ItemStack(BCTransportItems.pipeStructure, 3), possible)) {
            return ImmutableList.of();
        }
        List<AssemblyRecipe> recipes = new ArrayList<>();
        for (ItemStack stack : possible) {
            stack = stack.copy();
            stack.setCount(1);
            List<FacadeBlockStateInfo> infos = FacadeStateManager.stackFacades.get(new ItemStackKey(stack));
            if (infos == null || infos.isEmpty()) {
                continue;
            }
            for (FacadeBlockStateInfo info : infos) {
                addRecipe(recipes, stack, info);
            }
        }
        return recipes;
    }

    private static void addRecipe(List<AssemblyRecipe> recipes, ItemStack from, FacadeBlockStateInfo info) {
        ImmutableSet<StackDefinition> stacks = ImmutableSet.of(ArrayStackFilter.definition(from),
                ArrayStackFilter.definition(3, BCTransportItems.pipeStructure));

        NBTTagCompound recipeTag = new NBTTagCompound();
        recipeTag.setTag("stack", from.serializeNBT());

        String name = String.format("facade-normal-%s", info.state);
        recipes.add(new AssemblyRecipe(new ResourceLocation(BCTransport.MODID, name), MJ_COST, stacks, createFacadeStack(info, false), recipeTag));
        name = String.format("facade-hollow-%s", info.state);
        recipes.add(new AssemblyRecipe(new ResourceLocation(BCTransport.MODID, name), MJ_COST, stacks, createFacadeStack(info, true), recipeTag));
    }

    public static ItemStack createFacadeStack(FacadeBlockStateInfo info, boolean isHollow) {
        ItemStack stack = BCTransportItems.plugFacade.createItemStack(FullFacadeInstance.createSingle(info, isHollow));
        stack.setCount(6);
        return stack;
    }

    @Override
    public ChangingItemStack[] getRecipeInputs() {
        ChangingItemStack[] inputs = new ChangingItemStack[2];
        inputs[0] = ChangingItemStack.create(new ItemStack(BCTransportItems.pipeStructure, 3));
        NonNullList<ItemStack> list = NonNullList.create();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                list.add(info.requiredStack);
                list.add(info.requiredStack);
            }
        }
        inputs[1] = new ChangingItemStack(list);
        inputs[1].setTimeGap(TIME_GAP);
        return inputs;
    }

    @Override
    public ChangingItemStack getRecipeOutputs() {
        NonNullList<ItemStack> list = NonNullList.create();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                list.add(createFacadeStack(info, false));
                list.add(createFacadeStack(info, true));
            }
        }
        ChangingItemStack changing = new ChangingItemStack(list);
        changing.setTimeGap(TIME_GAP);
        return changing;
    }

    @Override
    public ChangingObject<Long> getMjCost() {
        return MJ_COSTS;
    }

    @Override
    public Optional<AssemblyRecipe> getRecipe(@Nonnull ResourceLocation name, @Nullable NBTTagCompound recipeTag) {
        if (!name.getResourceDomain().equals(BCTransport.MODID) || !name.getResourcePath().startsWith("facade-") ||
                recipeTag == null || !recipeTag.hasKey("stack")) return Optional.empty();
        ItemStack stack = new ItemStack(recipeTag.getCompoundTag("stack"));
        List<FacadeBlockStateInfo> infos = FacadeStateManager.stackFacades.get(new ItemStackKey(stack));
        if (infos == null || infos.isEmpty()) {
            return Optional.empty();
        }
        for (FacadeBlockStateInfo info : infos) {
            List<AssemblyRecipe> recipes = new ArrayList<>();
            addRecipe(recipes, stack, info);
            Optional<AssemblyRecipe> recipe = recipes.stream().filter(r -> name.equals(r.name)).findFirst();
            if (recipe.isPresent()) return recipe;
        }
        return Optional.empty();
    }
}
