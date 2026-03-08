/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.inventory.IItemTransactor.IItemExtractable;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/** Provides an {@link IItemTransactor} that cannot be inserted or extracted from directly, but implements
 * {@link IItemExtractable} so as to be noticed by pipes (and other machines) as one that will auto-insert into it. */
public enum AutomaticProvidingTransactor implements IItemExtractable {
    INSTANCE;

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }
}
