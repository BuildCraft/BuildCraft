/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/ */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.NonNullMatrix;

public class GuideCraftingFactory implements GuidePartFactory {

    private final NonNullMatrix<Ingredient> input;
    private final @Nonnull ItemStack output;
    private final int hash;

    public GuideCraftingFactory(Ingredient[][] input, ItemStack output) {
        this.input = new NonNullMatrix<>(input, Ingredient.EMPTY);
        this.output = StackUtil.asNonNull(output);
        NBTTagList hashNbt = new NBTTagList();
        for (Ingredient ingredient : this.input) {
            NBTTagList list = new NBTTagList();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                list.appendTag(stack.serializeNBT());
            }
            hashNbt.appendTag(list);
        }
        this.hash = hashNbt.hashCode();
    }

    public static GuidePartFactory create(@Nonnull ItemStack stack) {
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (OreDictionary.itemMatches(stack, StackUtil.asNonNull(recipe.getRecipeOutput()), false)) {
                GuidePartFactory val = getFactory(recipe);
                if (val != null) {
                    return val;
                } else {
                    BCLog.logger.warn("[lib.guide.crafting] Found a matching recipe, but of an unknown "
                        + recipe.getClass() + " for " + stack.getDisplayName());
                }
            }
        }
        return null;
    }

    public static GuidePartFactory getFactory(IRecipe recipe) {
        ItemStack output = recipe.getRecipeOutput();
        NonNullList<Ingredient> input = recipe.getIngredients();
        if (input == null || input.isEmpty() || output.isEmpty()) {
            return null;
        }
        Ingredient[][] matrix = new Ingredient[3][3];
        int maxX = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : 3;
        int maxY = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeHeight() : 3;
        int offsetX = maxX == 1 ? 1 : 0;
        int offsetY = maxY == 1 ? 1 : 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < offsetX || y < offsetY) {
                    matrix[x][y] = Ingredient.EMPTY;
                    continue;
                }
                int i = x - offsetX + (y - offsetY) * maxX;
                if (i >= input.size() || x - offsetX >= maxX) {
                    matrix[x][y] = Ingredient.EMPTY;
                } else {
                    matrix[x][y] = input.get(i);
                }
            }
        }
        return new GuideCraftingFactory(matrix, output);
    }

    @Nonnull
    private static ItemStack oreConvert(Object object) {
        if (object == null) {
            return StackUtil.EMPTY;
        }
        if (object instanceof ItemStack) {
            return ((ItemStack) object).copy();
        }
        if (object instanceof String) {
            NonNullList<ItemStack> stacks = OreDictionary.getOres((String) object);
            // It will be sorted out below
            object = stacks;
        }
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                return StackUtil.EMPTY;
            }
            Object first = list.get(0);
            if (first == null) {
                return StackUtil.EMPTY;
            }
            if (first instanceof ItemStack) {
                ItemStack best = (ItemStack) first;
                for (Object obj : list) {
                    if (!(obj instanceof ItemStack)) {
                        continue;
                    }
                    ItemStack stack = (ItemStack) obj;
                    // The lower the ID of an item, the closer it is to minecraft. Hmmm.
                    if (Item.getIdFromItem(stack.getItem()) < Item.getIdFromItem(best.getItem())) {
                        best = stack;
                    }
                }
                return best.copy();
            }
            BCLog.logger.warn("Found a list with unknown contents! " + first.getClass());
        }
        BCLog.logger.warn("Found an ore with an unknown " + object.getClass());
        return StackUtil.EMPTY;
    }

    public static GuidePartFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideCrafting createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
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
        GuideCraftingFactory other = (GuideCraftingFactory) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        if (hash != other.hash) return false;
        if (input.getWidth() != other.input.getWidth() || input.getHeight() != other.input.getHeight()) return false;
        NBTTagList nbtThis = new NBTTagList();
        for (Ingredient ingredient : this.input) {
            NBTTagList list = new NBTTagList();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                list.appendTag(stack.serializeNBT());
            }
            nbtThis.appendTag(list);
        }
        NBTTagList nbtThat = new NBTTagList();
        for (Ingredient ingredient : other.input) {
            NBTTagList list = new NBTTagList();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                list.appendTag(stack.serializeNBT());
            }
            nbtThat.appendTag(list);
        }
        return nbtThis.equals(nbtThat);
    }
}
