/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.recipe;

import buildcraft.api.BCItems;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import buildcraft.lib.recipe.IRecipeViewable;
import buildcraft.lib.recipe.assembly.AssemblyRecipe;
import buildcraft.lib.recipe.assembly.IFacadeAssemblyRecipes;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import buildcraft.silicon.plug.FacadeStateManager;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FacadeAssemblyRecipes extends AssemblyRecipe implements IRecipeViewable.IRecipePowered, IFacadeAssemblyRecipes {
    public static final FacadeAssemblyRecipes INSTANCE = new FacadeAssemblyRecipes();

//    static {
//        INSTANCE.setRegistryName(new ResourceLocation("buildcrafttransport:facade_recipes"));
//    }

    private static final int TIME_GAP = 500;
    private static final long MJ_COST = 64 * MjAPI.MJ;
    private static final ChangingObject<Long> MJ_COSTS = new ChangingObject<>(new Long[] { MJ_COST });

    public FacadeAssemblyRecipes() {
        name = new ResourceLocation("buildcrafttransport:facade_recipes");
    }

    public static ItemStack createFacadeStack(FacadeBlockStateInfo info, boolean isHollow) {
        ItemStack stack = ((IFacadeItem) BCItems.Silicon.PLUG_FACADE).createItemStack(FacadeInstance.createSingle(info, isHollow));
        stack.setCount(6);
        return stack;
    }

    @Override
    public ChangingItemStack[] getRecipeInputs() {
        ChangingItemStack[] inputs = new ChangingItemStack[2];
        inputs[0] = new ChangingItemStack(baseRequirementStack());
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
    public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
        if (!StackUtil.contains(baseRequirementStack(), inputs)) {
            return Collections.emptySet();
        }

        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : inputs) {
            stack = stack.copy();
            stack.setCount(1);
            List<FacadeBlockStateInfo> infos = FacadeStateManager.stackFacades.get(new ItemStackKey(stack));
            if (infos == null || infos.isEmpty()) {
                continue;
            }
            for (FacadeBlockStateInfo info : infos) {
                stacks.add(createFacadeStack(info, false));
                stacks.add(createFacadeStack(info, true));
            }
        }
        return ImmutableSet.copyOf(stacks);
    }

    private static ItemStack baseRequirementStack() {
//        if (BCItems.Transport.PIPE_STRUCTURE == null)
        if (BCItems.Transport.PIPE_STRUCTURE_COBBLESTONE_COLORLESS == null) {
            return new ItemStack(Blocks.COBBLESTONE_WALL);
        }
//        return new ItemStack(BCTransportItems.PIPE_STRUCTURE, 3);
        return new ItemStack(BCItems.Transport.PIPE_STRUCTURE_COBBLESTONE_COLORLESS, 3);
    }

    @Override
    public Set<ItemStack> getOutputPreviews() {
        return Collections.emptySet();
    }

    @Override
    public Set<IngredientStack> getInputsFor(@Nonnull ItemStack output) {
        FacadePhasedState state = ItemPluggableFacade.getStates(output).getCurrentStateForStack();
        ItemStack stateRequirement = state.stateInfo.requiredStack;
        IngredientStack ingredientType = new IngredientStack(Ingredient.of(stateRequirement));
        IngredientStack ingredientBase = new IngredientStack(Ingredient.of(baseRequirementStack()), 3);

        return ImmutableSet.of(ingredientType, ingredientBase);
    }

    @Override
    public long getRequiredMicroJoulesFor(@Nonnull ItemStack output) {
        return MJ_COST;
    }

    @Override
    public long getRequiredMicroJoules() {
        return MJ_COST;
    }

    @Override
    public Set<IngredientStack> getRequiredIngredientStacks() {
        return ImmutableSet.of();
    }

    @Override
    public Set<ItemStack> getOutput() {
        return ImmutableSet.of();
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return StackUtil.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return StackUtil.EMPTY;
    }
}
