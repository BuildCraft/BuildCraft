/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class GuideSmeltingFactory implements GuidePartFactory {
    @Nonnull
//    private final ItemStack input;
    private final NonNullList<Ingredient> input;
    private final ItemStack output;
    private final int hash;

    // public GuideSmeltingFactory(ItemStack input, ItemStack output)
    public GuideSmeltingFactory(NonNullList<Ingredient> input, ItemStack output) {
//        this.input = StackUtil.asNonNull(input);
        this.input = input;
        this.output = StackUtil.asNonNull(output);
//        this.hash = Arrays.hashCode(new int[] { input.serializeNBT().hashCode(), output.serializeNBT().hashCode() });
        this.hash = Arrays.hashCode(new int[] { input.hashCode(), output.serializeNBT().hashCode() });
    }

    public static GuideSmeltingFactory create(ItemStack stack) {
//        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
        for (SmeltingRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
//            if (ItemStack.areItemsEqual(stack, entry.getValue()))
            if (StackUtil.isSameItemSameDamage(stack, recipe.getResultItem(Minecraft.getInstance().level.registryAccess()))) {
//                return new GuideSmeltingFactory(entry.getKey(), stack);
                return new GuideSmeltingFactory(recipe.getIngredients(), stack);
            }
        }
        return null;
    }

    public static GuideSmeltingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
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
        GuideSmeltingFactory other = (GuideSmeltingFactory) obj;
        // Shortcut out of this full itemstack comparison as its really expensive
        if (hash != other.hash) return false;

//        return ItemStack.areItemStacksEqual(input, other.input)//
        return input.equals(other.input)//
//                && ItemStack.areItemStacksEqual(output, other.output);
                && ItemStack.matches(output, other.output);
    }
}
