/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.ItemUtil;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public final class ChangingItemStack extends ChangingObject<ItemStackKey> {
    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     *
     * @param stacks The list to iterate through. */
//    public ChangingItemStack(NonNullList<ItemStack> stacks)
    // Calen: ? is ItemStack or Ingredient
    public ChangingItemStack(NonNullList<?> stacks) {
        super(makeListArray(stacks));
    }

    public ChangingItemStack(@Nonnull Ingredient ingredient) {
        super(makeRecipeArray(ingredient));
    }

    public ChangingItemStack(ItemStack stack) {
        super(makeStackArray(stack));
    }

    // public ChangingItemStack(String oreId)
    public ChangingItemStack(TagKey<Item> oreId) {
//        this(OreDictionary.getOres(oreId));
        this(NonNullList.of(ForgeRegistries.ITEMS.tags().getTag(oreId).stream().map(ItemStack::new).toArray(ItemStack[]::new)));
    }

    // private static ItemStackKey[] makeListArray(NonNullList<ItemStack> stacks)
    private static ItemStackKey[] makeListArray(NonNullList<?> items) {
//        return makeStackArray(stacks.toArray(new ItemStack[0]));
        List<ItemStack> ret = Lists.newArrayList();
        for (Object ele : items) {
            if (ele instanceof ItemStack stack) {
                ret.add(stack);
            } else if (ele instanceof Ingredient ingredient) {
                Arrays.stream(ingredient.getItems()).forEach(ret::add);
            } else {
                throw new RuntimeException("[lib.guide.recipe] Recipe items should be ItemStack or Ingredient!");
            }
        }
//        return makeStackArray(stacks.toArray(new ItemStack[0]));
        return makeStackArray(ret.toArray(new ItemStack[0]));
    }

    private static ItemStackKey[] makeStackArray(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStackKey[] { ItemStackKey.EMPTY };
        }
//        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
        if (stack.getDamageValue() == Short.MAX_VALUE) {
            NonNullList<ItemStack> subs = NonNullList.create();
            ItemUtil.fillItemCategory(stack.getItem(), CreativeModeTabs.SEARCH, subs);
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
