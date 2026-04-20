/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

import javax.annotation.Nonnull;

import ct.buildcraft.lib.misc.ItemStackKey;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public final class ChangingItemStack extends ChangingObject<ItemStackKey> {
    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     * 
     * @param stacks The list to iterate through. */
    public ChangingItemStack(NonNullList<ItemStack> stacks) {
        super(makeListArray(stacks));
    }

    public ChangingItemStack(@Nonnull Ingredient ingredient) {
        super(makeRecipeArray(ingredient));
    }

    @Deprecated
    public ChangingItemStack(ItemStack stack) {
        super(makeStackArray(stack));
    }

    public ChangingItemStack(TagKey<Item> tag) {
        this(NonNullList.of(ItemStack.EMPTY, new Ingredient.TagValue(tag).getItems().toArray(new ItemStack[1])));
    }

    private static ItemStackKey[] makeListArray(NonNullList<ItemStack> stacks) {
        return makeStackArray(stacks.toArray(new ItemStack[0]));
    }

    private static ItemStackKey[] makeStackArray(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStackKey[] { ItemStackKey.EMPTY };
        }
        if (stack.is(Tags.Items.ORES)) {
            NonNullList<ItemStack> subs = NonNullList.create();
            //stack.getItem().getSubItems(CreativeModeTab.TAB_SEARCH, subs);//TODO
            return makeListArray(subs);
        } else {
            return new ItemStackKey[] { new ItemStackKey(stack) };
        }
    }

    private static ItemStackKey[] makeRecipeArray(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
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
