/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.lib.misc.StackUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Returns true if the stack matches any one one of the filter stacks. Checks the OreDictionary and wildcards. */
public class CraftingFilter implements IStackFilter {

    private final NonNullList<ItemStack> stacks;

    public CraftingFilter(ItemStack... stacks) {
        this.stacks = StackUtil.listOf(stacks);
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }
        for (ItemStack stack1 : stacks) {
            if (StackUtil.isCraftingEquivalent(stack1, stack, true)) {
                return true;
            }
        }
        return false;
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public boolean hasFilter() {
        for (ItemStack filter : stacks) {
            if (filter != null) {
                return true;
            }
        }
        return false;
    }
}
