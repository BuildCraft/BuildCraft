/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import javax.annotation.Nonnull;
import java.util.List;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

// STUB(R.Chen): OreDictionaryStub removed — TODO(R.Chen): implement via Tags

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public final class ChangingItemStack extends ChangingObject<ItemStackKey> {
    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     * 
     * @param stacks The list to iterate through. */
    public ChangingItemStack(DefaultedList<ItemStack> stacks) {
        super(makeListArray(stacks));
    }

    public ChangingItemStack(@Nonnull Ingredient ingredient) {
        super(makeRecipeArray(ingredient));
    }

    public ChangingItemStack(ItemStack stack) {
        super(makeStackArray(stack));
    }

    public ChangingItemStack(String oreId) {
        this(OreDictionaryStub.getOres(oreId));
    }

    private static ItemStackKey[] makeListArray(DefaultedList<ItemStack> stacks) {
        return makeStackArray(stacks.toArray(new ItemStack[0]));
    }

    private static ItemStackKey[] makeStackArray(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStackKey[] { ItemStackKey.EMPTY };
        }
        if (stack.getDamage() == OreDictionaryStub.WILDCARD_VALUE) {
            DefaultedList<ItemStack> subs = DefaultedList.of();
            stack.getItem().getSubItems(ItemGroup.SEARCH, subs);
            return makeListArray(subs);
        } else {
            return new ItemStackKey[] { new ItemStackKey(stack) };
        }
    }

    private static ItemStackKey[] makeRecipeArray(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        return makeStackArray(stacks);
    }

    private static ItemStackKey[] makeStackArray(ItemStack[] stacks) {
        if (stacks.length == 0) {
            return new ItemStackKey[] { ItemStackKey.EMPTY };
        } else {
            ItemStackKey[] arr = new ItemStackKey[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                arr[i] = new ItemStackKey(stacks[i]);
            }
            return arr;
        }
    }

    public boolean matches(ItemStack target) {
        for (ItemStackKey s : options) {
            if (StackUtil.isCraftingEquivalent(s.baseStack, target, false)) {
                return true;
            }
        }
        return false;
    }
}
