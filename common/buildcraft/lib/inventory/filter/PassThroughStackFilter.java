/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/** Returns true if the stack matches any one one of the filter stacks. */
public class PassThroughStackFilter implements IStackFilter {

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() > 0;
    }

}
