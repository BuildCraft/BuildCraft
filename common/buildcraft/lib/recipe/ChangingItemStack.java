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

    /** Creates a changing item stack that iterates through all sub-item variants of the specified stack, if its
     * metadata is equal to {@link OreDictionary#WILDCARD_VALUE}
     * 
     * @param stack the stack to check. */
    public static ChangingItemStack create(ItemStack stack) {
        if (BCStackHelper.isEmpty(stack)) {
            return new ChangingItemStack(StackUtil.listOf(null));
        } else if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> subs = Lists.newArrayList();
            stack.getItem().getSubItems(stack.getItem(), null, subs);
            return new ChangingItemStack(subs);
        } else {
            return new ChangingItemStack(StackUtil.listOf(stack));
        }
    }

    public ChangingItemStack(String oreId) {
        this(OreDictionary.getOres(oreId));
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
