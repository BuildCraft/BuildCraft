/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): InventoryWrapper deferred
package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

public final class InventoryWrapper extends AbstractInvItemTransactor {

    private final net.minecraft.entity.player.PlayerInventory inventory;

    public InventoryWrapper(net.minecraft.entity.player.PlayerInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        // STUB
        return stack;
    }

    @Override
    protected ItemStack extract(int slot, buildcraft.lib.inventory.filter.IStackFilter filter, int min, int max, boolean simulate) {
        // STUB
        return ItemStack.EMPTY;
    }

    @Override
    protected int getSlots() {
        return inventory.size();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return inventory.getStack(slot).isEmpty();
    }
}
