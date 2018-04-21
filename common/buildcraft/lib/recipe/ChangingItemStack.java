/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.api.items.BCStackHelper;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.Lists;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

/** Defines an {@link ItemStack} that changes between a specified list of stacks. Useful for displaying possible inputs
 * or outputs for recipes that use the oredictionary, or recipes that vary the output depending on the metadata of the
 * input (for example a pipe colouring recipe) */
public final class ChangingItemStack extends ChangingObject<ItemStack>{
    /** Creates a stack list that iterates through all of the given stacks. This does NOT check possible variants.
     *
     * @param stacks The list to iterate through. */
    public ChangingItemStack(List<ItemStack> stacks) {
        super(stacks.toArray(new ItemStack[0]));
    }

    public ChangingItemStack(ItemStack stack) {
        super(makeStackArray(stack));
    }

    public ChangingItemStack(String oreId) {
        this(OreDictionary.getOres(oreId));
    }

    private static ItemStack[] makeStackArray(ItemStack stack) {
        if (BCStackHelper.isEmpty(stack)) {
            return new ItemStack[] { null };
        }
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> subs = Lists.newLinkedList();
            stack.getItem().getSubItems(stack.getItem(), CreativeTabs.SEARCH, subs);
            return subs.toArray(new ItemStack[0]);
        } else {
            return new ItemStack[] { stack };
        }
    }

    private static ItemStack[] makeRecipeArray(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return new ItemStack[] { null };
        } else {
            ItemStack[] stackArray = new ItemStack[stacks.size()];
            stacks.toArray(stackArray);
            return stackArray;
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
