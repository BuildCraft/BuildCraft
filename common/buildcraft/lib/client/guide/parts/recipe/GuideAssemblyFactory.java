/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class GuideAssemblyFactory implements GuidePartFactory {
    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;
    private final int hash;

    public GuideAssemblyFactory(ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
        this.hash = computeHash();
    }

    public GuideAssemblyFactory(ItemStack[] input, ItemStack output, long mjCost) {
        this.input = ArrayUtil.map(input, ChangingItemStack::new, ChangingItemStack[]::new);
        this.output = new ChangingItemStack(output);
        this.mjCost = new ChangingObject<>(new Long[] { mjCost });
        this.hash = computeHash();
    }

    private int computeHash() {
        return Arrays.deepHashCode(new Object[] { input, output, mjCost });
    }

    // public static GuideAssemblyFactory create(@Nonnull ItemStack stack) {
    // for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
    // if (OreDictionary.itemMatches(stack, StackUtil.asNonNull(recipe.getRecipeOutput()), false)) {
    // GuideAssemblyFactory val = getFactory(recipe);
    // if (val != null) {
    // return val;
    // } else {
    // BCLog.logger.warn("[lib.guide.crafting] Found a matching recipe, but of an unknown " + recipe.getClass() + " for
    // " + stack.getDisplayName());
    // }
    // }
    // }
    // return null;
    // }

    // public static GuideAssemblyFactory getFactory(IRecipe recipe) {
    // GuideAssemblyFactory val = null;
    // if (recipe instanceof ShapedRecipes) {
    // ShapedRecipes shaped = (ShapedRecipes) recipe;
    // ItemStack[] input = shaped.recipeItems;
    // ItemStack[][] dimInput = new ItemStack[shaped.recipeWidth][shaped.recipeHeight];
    // for (int x = 0; x < dimInput.length; x++) {
    // for (int y = 0; y < dimInput[x].length; y++) {
    // dimInput[x][y] = input[x + y * dimInput.length].copy();
    // }
    // }
    // val = new GuideAssemblyFactory(dimInput, recipe.getRecipeOutput());
    // } else if (recipe instanceof ShapedOreRecipe) {
    // Object[] input = ((ShapedOreRecipe) recipe).getInput();
    // ItemStack[][] dimInput = getStackSizeArray(recipe);
    // for (int x = 0; x < dimInput.length; x++) {
    // for (int y = 0; y < dimInput[x].length; y++) {
    // dimInput[x][y] = oreConvert(input[x + y * dimInput.length]);
    // }
    // }
    // val = new GuideAssemblyFactory(dimInput, recipe.getRecipeOutput());
    // } else if (recipe instanceof ShapelessOreRecipe) {
    // List<Object> input = ((ShapelessOreRecipe) recipe).getInput();
    // ItemStack[][] dimInput = getStackSizeArray(recipe);
    // for (int x = 0; x < dimInput.length; x++) {
    // for (int y = 0; y < dimInput[x].length; y++) {
    // int index = x + y * dimInput.length;
    // if (index < input.size()) {
    // dimInput[x][y] = oreConvert(input.get(index));
    // }
    // }
    // }
    // val = new GuideAssemblyFactory(dimInput, recipe.getRecipeOutput());
    // } else if (recipe instanceof ShapelessRecipes) {
    // List<ItemStack> input = ((ShapelessRecipes) recipe).recipeItems;
    // ItemStack[][] dimInput = getStackSizeArray(recipe);
    // for (int x = 0; x < dimInput.length; x++) {
    // for (int y = 0; y < dimInput[x].length; y++) {
    // int index = x + y * dimInput.length;
    // if (index < input.size()) {
    // dimInput[x][y] = input.get(index).copy();
    // }
    // }
    // }
    // val = new GuideAssemblyFactory(dimInput, recipe.getRecipeOutput());
    // } else if (recipe instanceof IRecipeViewable) {
    // // TODO: Implement IRecipeViewable usage
    // } else {
    // BCLog.logger.warn("[lib.guide.crafting] Found an unknown recipe " + recipe.getClass());
    // }
    // return val;
    // }

    // private static ItemStack[][] getStackSizeArray(IRecipe recipe) {
    // if (recipe instanceof ShapedRecipes) {
    // return new ItemStack[((ShapedRecipes) recipe).recipeWidth][((ShapedRecipes) recipe).recipeHeight];
    // } else if (recipe instanceof ShapedOreRecipe) {
    // // YAAAAY REFLECTION :(
    // int width = 3;
    // int height = 3;
    // try {
    // width = SHAPED_ORE_RECIPE___WIDTH.getInt(recipe);
    // height = SHAPED_ORE_RECIPE___HEIGHT.getInt(recipe);
    // } catch (Throwable t) {
    // BCLog.logger.error("Could not access the required shaped ore recipe fields!", t);
    // }
    // return new ItemStack[width][height];
    // }
    // return new ItemStack[3][3];
    // }

    // @Nonnull
    // private static ItemStack oreConvert(Object object) {
    // if (object == null) {
    // return StackUtil.EMPTY;
    // }
    // if (object instanceof ItemStack) {
    // return ((ItemStack) object).copy();
    // }
    // if (object instanceof String) {
    // NonNullList<ItemStack> stacks = OreDictionary.getOres((String) object);
    // // It will be sorted out below
    // object = stacks;
    // }
    // if (object instanceof List<?>) {
    // List<?> list = (List<?>) object;
    // if (list.isEmpty()) {
    // return StackUtil.EMPTY;
    // }
    // Object first = list.get(0);
    // if (first == null) {
    // return StackUtil.EMPTY;
    // }
    // if (first instanceof ItemStack) {
    // // Technically a safe cast as the first one WAS an Item Stack and we never add to the list
    // @SuppressWarnings("unchecked")
    // NonNullList<ItemStack> stacks = (NonNullList<ItemStack>) list;
    // if (stacks.size() == 0) {
    // return StackUtil.EMPTY;
    // }
    // ItemStack best = stacks.get(0);
    // for (ItemStack stack : stacks) {
    // // The lower the ID of an item, the closer it is to minecraft. Hmmm.
    // if (Item.getIdFromItem(stack.getItem()) < Item.getIdFromItem(best.getItem())) {
    // best = stack;
    // }
    // }
    // return best.copy();
    // }
    // BCLog.logger.warn("Found a list with unknown contents! " + first.getClass());
    // }
    // BCLog.logger.warn("Found an ore with an unknown " + object.getClass());
    // return StackUtil.EMPTY;
    // }

    // public static GuideAssemblyFactory create(Item output) {
    // return create(new ItemStack(output));
    // }

    @Override
    public GuideAssembly createNew(GuiGuide gui) {
        return new GuideAssembly(gui, input, output, mjCost);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideAssemblyFactory other = (GuideAssemblyFactory) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        if (hash != other.hash) return false;
        if (input.length != other.input.length) return false;
        return Arrays.equals(input, other.input) && output.equals(other.output);
    }
}
