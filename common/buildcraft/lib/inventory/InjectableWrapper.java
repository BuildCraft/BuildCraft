/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nullable;

import buildcraft.lib.item.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

import java.util.List;

public class InjectableWrapper implements IItemTransactor {
    private final IInjectable injectable;
    private final EnumFacing from;

    public InjectableWrapper(IInjectable injectable, EnumFacing facing) {
        this.injectable = injectable;
        this.from = facing;
    }

    @Nullable
    @Override
    public ItemStack insert(@Nullable ItemStack stack, boolean allOrNone, boolean simulate) {
        if (ItemStackHelper.isEmpty(stack))
            return null;
        if (allOrNone) {
            stack = stack.copy();
            ItemStack leftOver = injectable.injectItem(stack, false, from, null, 0);
            if (ItemStackHelper.isEmpty(leftOver)) {
                ItemStack reallyLeftOver = injectable.injectItem(stack, !simulate, from, null, 0);
                // sanity check: it really helps debugging
                if (!ItemStackHelper.isEmpty(reallyLeftOver)) {
                    throw new IllegalStateException("Found an invalid IInjectable instance! (leftOver = "//
                        + leftOver + ", reallyLeftOver = " + reallyLeftOver + ", " + injectable.getClass() + ")");
                } else {
                    return null;
                }
            } else {
                return stack;
            }
        } else {
            return injectable.injectItem(stack, !simulate, from, null, 0);
        }
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, boolean simulate) {
        return ItemTransactorHelper.insertAllBypass(this, stacks, simulate);
    }

    @Nullable
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        return null;
    }
}
