/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public final class ChangingItemStack extends ChangingObject<ItemStack> {
    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     * 
     * @param stacks The list to iterate through. */
    public ChangingItemStack(NonNullList<ItemStack> stacks) {
        super(stacks.toArray(new ItemStack[0]));
    }

    public ChangingItemStack(@Nonnull Ingredient ingredient) {
        super(makeRecipeArray(ingredient));
    }

    public ChangingItemStack(ItemStack stack) {
        super(makeStackArray(stack));
    }

    public ChangingItemStack(String oreId) {
        this(OreDictionary.getOres(oreId));
    }

    private static ItemStack[] makeStackArray(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStack[] { ItemStack.EMPTY };
        }
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            NonNullList<ItemStack> subs = NonNullList.create();
            stack.getItem().getSubItems(CreativeTabs.SEARCH, subs);
            return subs.toArray(new ItemStack[0]);
        } else {
            return new ItemStack[] { stack };
        }
    }

    private static ItemStack[] makeRecipeArray(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length == 0) {
            return new ItemStack[] { ItemStack.EMPTY };
        } else {
            return stacks.clone();
        }
    }

    @Override
    protected int computeHash() {
        return ArrayUtil.manualHash(options, StackUtil::hash);
    }

    public boolean matches(ItemStack target) {
        for (ItemStack s : options) {
            if (StackUtil.isCraftingEquivalent(s, target, false)) {
                return true;
            }
        }
        return false;
    }
}
