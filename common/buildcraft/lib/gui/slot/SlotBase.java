/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.slot;

import javax.annotation.Nonnull;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;

// STUB(R.Chen): Forge SlotItemHandler → Fabric Slot. SimpleInventory used as constructor placeholder;
// real item access routes through IItemHandlerAdv. Full Transfer-API slot wiring deferred.
public class SlotBase extends Slot {
    public final int handlerIndex;
    public final IItemHandlerAdv itemHandler;

    public SlotBase(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(new SimpleInventory(Math.max(1, itemHandler.getSlots())), slotIndex, posX, posY);
        this.handlerIndex = slotIndex;
        this.itemHandler = itemHandler;
    }

    public boolean canShift() {
        return true;
    }

    @Override
    public ItemStack getStack() {
        return itemHandler.getStackInSlot(handlerIndex);
    }

    @Override
    public void setStack(ItemStack stack) {
        itemHandler.setStackInSlot(handlerIndex, stack);
        markDirty();
    }

    @Override
    public boolean canInsert(@Nonnull ItemStack stack) {
        return itemHandler.canSet(handlerIndex, stack);
    }

    public ItemStack insert(ItemStack stack, boolean simulate) {
        return itemHandler.insertItem(handlerIndex, stack, simulate);
    }
}
