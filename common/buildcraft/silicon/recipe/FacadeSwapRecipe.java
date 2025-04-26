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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public enum FacadeSwapRecipe implements ICraftingRecipe, IRecipeViewable.IViewableGrid {
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
    public boolean matches(CraftingInventory inv, World world) {
//        return !getCraftingResult(inv).isEmpty();
        return !assemble(inv).isEmpty();
    }

    @Nonnull
    @Override
//    public ItemStack getCraftingResult(InventoryCrafting inv)
    public ItemStack assemble(CraftingInventory inv) {
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
        if (BCSiliconItems.plugFacade == null || !BCSiliconItems.plugFacade.isPresent() || stackIn.getItem() != BCSiliconItems.plugFacade.get()) {
            return StackUtil.EMPTY;
        }
        FacadeInstance states = ItemPluggableFacade.getStates(stackIn);
        states = states.withSwappedIsHollow();
        return BCSiliconItems.plugFacade.get().createItemStack(states);
    }

    @Nonnull
    @Override
//    public ItemStack getRecipeOutput()
    public ItemStack getResultItem() {
        return StackUtil.EMPTY;
    }

    // Calen use default getRemainingItems in IRecipe.class
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

    @Nonnull
    @Override
//    public ResourceLocation getRegistryName()
    public ResourceLocation getId() {
        return new ResourceLocation(BCTransport.MODID, "facade_swap");
    }

    @Nonnull
    @Override
    public IRecipeSerializer<FacadeSwapRecipe> getSerializer() {
        return FacadeSwapRecipeSerializer.INSTANCE;
    }

    @Nonnull
    @Override
//    public Class<IRecipe> getRegistryType()
    public IRecipeType<ICraftingRecipe> getType() {
//        return IRecipe.class;
        return IRecipeType.CRAFTING;
    }

    @Override
//    public boolean canFit(int width, int height)
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

}
