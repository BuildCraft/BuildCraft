/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.recipe;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.transport.BCTransport;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


public enum FacadeSwapRecipe implements CraftingRecipe, IRecipeViewable.IViewableGrid {
    INSTANCE;

    public static final ResourceLocation TYPE_ID = new ResourceLocation(BCSilicon.MODID, "facade_swap");

    private static final int TIME_GAP = 500;

    private static final ChangingItemStack[] INPUTS = { null };
    private static ChangingItemStack OUTPUTS;

    public static void genRecipes() {
        if (FacadeAPI.facadeItem == null) {
            throw new IllegalStateException("Don't call FacadeSwapRecipe if the facade item doesn't exist!");
        }
        NonNullList<ItemStack> list1 = NonNullList.create();
        NonNullList<ItemStack> list2 = NonNullList.create();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                ItemStack stack = createFacade(info, false);
                ItemStack stackHollow = createFacade(info, true);
                list1.add(stack);
                list1.add(stackHollow);
                list2.add(stackHollow);
                list2.add(stack);
            }
        }
        if (!list1.isEmpty()) {
            INPUTS[0] = new ChangingItemStack(list1);
            INPUTS[0].setTimeGap(TIME_GAP);

            OUTPUTS = new ChangingItemStack(list2);
            OUTPUTS.setTimeGap(TIME_GAP);
        }
    }

    @Override
//    public boolean matches(InventoryCrafting inv, World world)
    public boolean matches(CraftingContainer inv, Level world) {
//        return !getCraftingResult(inv).isEmpty();
        return !assemble(inv, null).isEmpty();
    }

    @NotNull
    @Override
//    public ItemStack getCraftingResult(InventoryCrafting inv)
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack stackIn = StackUtil.EMPTY;
        for (int s = 0; s < inv.getContainerSize(); s++) {
            ItemStack stack = inv.getItem(s);
            if (!stack.isEmpty()) {
                if (stackIn.isEmpty()) {
                    stackIn = stack;
                } else {
                    return StackUtil.EMPTY;
                }
            }
        }
        if (stackIn.getItem() != BCSiliconItems.plugFacade.get()) {
            return StackUtil.EMPTY;
        }
        FacadeInstance states = ItemPluggableFacade.getStates(stackIn);
        states = states.withSwappedIsHollow();
        return BCSiliconItems.plugFacade.get().createItemStack(states);
    }

    @NotNull
    @Override
//    public ItemStack getRecipeOutput()
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return StackUtil.EMPTY;
    }

    // Calen use default getRemainingItems in Recipe.class
//    @Override
//    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
//        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
//    }

    @Override
    public ChangingItemStack[] getRecipeInputs() {
        if (INPUTS[0] == null) {
            genRecipes();
        }
        return INPUTS;
    }

    @Override
    public ChangingItemStack getRecipeOutputs() {
        if (OUTPUTS == null) {
            genRecipes();
        }
        return OUTPUTS;
    }

    private static ItemStack createFacade(FacadeBlockStateInfo info, boolean isHollow) {
        FacadeInstance state = FacadeInstance.createSingle(info, isHollow);
        return BCSiliconItems.plugFacade.get().createItemStack(state);
    }

    @Override
    public int getRecipeWidth() {
        return 1;
    }

    @Override
    public int getRecipeHeight() {
        return 1;
    }

//    @Override
//    public IRecipe setRegistryName(ResourceLocation name) {
//        return this;
//    }

    @NotNull
    @Override
//    public ResourceLocation getRegistryName()
    public ResourceLocation getId() {
        return new ResourceLocation(BCTransport.MODID, "facade_swap");
    }

    @NotNull
    @Override
    public RecipeSerializer<FacadeSwapRecipe> getSerializer() {
        return FacadeSwapRecipeSerializer.INSTANCE;
    }

    @NotNull
    @Override
//    public Class<Recipe> getRegistryType()
    public RecipeType<CraftingRecipe> getType() {
//        return Recipe.class;
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
//    public boolean canFit(int width, int height)
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

}
