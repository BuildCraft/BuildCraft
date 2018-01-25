/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import javax.annotation.Nullable;

import buildcraft.lib.item.ItemStackHelper;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

public class InvertedStackFilter implements IStackFilter {

    private final IStackFilter filter;

    public InvertedStackFilter(IStackFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean matches(@Nullable ItemStack stack) {
        return !ItemStackHelper.isEmpty(stack) && !filter.matches(stack);
    }
}
