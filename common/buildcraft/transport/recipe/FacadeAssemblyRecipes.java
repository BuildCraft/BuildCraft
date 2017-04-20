package buildcraft.transport.recipe;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.item.ItemStack;
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

public enum FacadeAssemblyRecipes implements IAssemblyRecipeProvider, IRecipeViewable.IRecipePowered {
    INSTANCE;

    private static final int TIME_GAP = 500;
    private static final long MJ_COST = 64 * MjAPI.MJ;
    private static final ChangingObject<Long> MJ_COSTS = new ChangingObject<>(new Long[] { MJ_COST });

    @Override
    public List<AssemblyRecipe> getRecipesFor(NonNullList<ItemStack> possible) {
        // Require 3 structure pipes -- check for those first as its much cheaper
        if (!StackUtil.contains(new ItemStack(BCTransportItems.pipeStructure, 3), possible)) {
            return ImmutableList.of();
        }
        List<AssemblyRecipe> recipes = new ArrayList<>();
        for (ItemStack stack : possible) {
            if (stack.getCount() < 6) {
                continue;
            }
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
        ItemStack req = from.copy();
        req.setCount(6);
        ImmutableSet<ItemStack> stacks = ImmutableSet.of(req, new ItemStack(BCTransportItems.pipeStructure, 3));

        recipes.add(new AssemblyRecipe(MJ_COST, stacks, createFacadeStack(info, false)));
        recipes.add(new AssemblyRecipe(MJ_COST, stacks, createFacadeStack(info, true)));
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
}
