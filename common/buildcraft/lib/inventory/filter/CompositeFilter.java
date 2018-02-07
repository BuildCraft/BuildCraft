/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

import java.util.List;

/** Returns true if the stack matches any one one of the filter stacks. */
public class CompositeFilter implements IStackFilter {

    private final IStackFilter[] filters;

    public CompositeFilter(IStackFilter... iFilters) {
        filters = iFilters;
    }

    @Nonnull
    @Override
    public List<ItemStack> getExamples() {
        List<ItemStack> stacks = Lists.newArrayList();
        for (IStackFilter filter : filters)
            stacks.addAll(filter.getExamples());
        return stacks;
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        for (IStackFilter f : filters) {
            if (f.matches(stack)) {
                return true;
            }
        }

        return false;
    }
}
