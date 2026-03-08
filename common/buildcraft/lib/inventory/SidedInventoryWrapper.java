/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public final class SidedInventoryWrapper extends AbstractInvItemTransactor {
    private final ISidedInventory sided;
    private final InventoryWrapper normal;
    private final Direction face;
    private final int[] slots;

    public SidedInventoryWrapper(ISidedInventory sided, Direction face) {
        this.sided = sided;
        this.normal = new InventoryWrapper(sided);
        this.face = face;
        slots = sided.getSlotsForFace(face);
    }

    @Nonnull
    @Override
    protected ItemStack insert(int externalSlot, @Nonnull ItemStack stack, boolean simulate) {
        int sidedSlot = slots[externalSlot];
//        if (sided.canInsertItem(sidedSlot, stack, face))
        if (sided.canPlaceItemThroughFace(sidedSlot, stack, face)) {
            // Delegate to the normal inserter - its just easier.
            return normal.insert(sidedSlot, stack, simulate);
        }
        return stack;
    }

    @Nonnull
    @Override
    protected ItemStack extract(int externalSlot, IStackFilter filter, int min, int max, boolean simulate) {
        int sidedSlot = slots[externalSlot];
//        ItemStack current = sided.getStackInSlot(sidedSlot);
        ItemStack current = sided.getItem(sidedSlot);
//        if (sided.canExtractItem(sidedSlot, current, face))
        if (sided.canTakeItemThroughFace(sidedSlot, current, face)) {
            // Delegate to the normal inserter - its just easier.
            return normal.extract(sidedSlot, filter, min, max, simulate);
        }
        return StackUtil.EMPTY;
    }

    @Override
    protected int getSlots() {
        return slots.length;
    }

    @Override
    protected boolean isEmpty(int slot) {
        return normal.isEmpty(slots[slot]);
    }
}
