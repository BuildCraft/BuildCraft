/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import ct.buildcraft.lib.misc.StackUtil;

import net.minecraft.world.item.ItemStack;

/** Returns true if the stack matches any one one of the filter stacks. Takes into account item lists. */
public class ArrayStackOrListFilter extends ArrayStackFilter {

    public ArrayStackOrListFilter(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        if (stacks.size() == 0 || !hasFilter()) {
            return true;
        }

        for (ItemStack s : stacks) {
            if (StackUtil.isMatchingItemOrList(s, stack)) {
                return true;
            }
        }

        return false;
    }
}
