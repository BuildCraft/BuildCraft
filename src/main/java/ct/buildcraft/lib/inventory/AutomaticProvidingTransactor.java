/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.inventory.IItemTransactor;
import ct.buildcraft.api.inventory.IItemTransactor.IItemExtractable;

import net.minecraft.world.item.ItemStack;

/** Provides an {@link IItemTransactor} that cannot be inserted or extracted from directly, but implements
 * {@link IItemExtractable} so as to be noticed by pipes (and other machines) as one that will auto-insert into it. */
public enum AutomaticProvidingTransactor implements IItemExtractable {
    INSTANCE;

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
