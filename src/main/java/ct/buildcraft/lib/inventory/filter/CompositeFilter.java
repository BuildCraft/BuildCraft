/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;

import net.minecraft.world.item.ItemStack;

/** Returns true if the stack matches any one one of the filter stacks. */
public class CompositeFilter implements IStackFilter {

    private final IStackFilter[] filters;

    public CompositeFilter(IStackFilter... iFilters) {
        filters = iFilters;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        for (IStackFilter f : filters) {
            if (f.matches(stack)) {
                return true;
            }
        }

        return false;
    }
}
