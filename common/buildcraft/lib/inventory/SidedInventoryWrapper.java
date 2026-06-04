/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ISidedInventory removed in 1.20.1 deferred
package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;

public final class SidedInventoryWrapper extends AbstractInvItemTransactor {
    public SidedInventoryWrapper(Object sided, Direction face) {}

    @Nonnull
    @Override
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        return StackUtil.EMPTY;
    }

    @Override
    protected int getSlots() {
        return 0;
    }

    @Override
    protected boolean isEmpty(int slot) {
        return true;
    }
}
