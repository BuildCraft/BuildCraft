/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

/** Returns true if the stack matches any one one of the filter stacks. */
public class PassThroughStackFilter implements IStackFilter {

    @Override
    public boolean matches(ItemStack stack) {
        return stack != null && stack.stackSize > 0;
    }

}
