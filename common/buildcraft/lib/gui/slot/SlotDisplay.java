/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import java.util.function.IntFunction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDisplay extends Slot {
    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return getter.apply(getSlotIndex()).copy();
    }

    @Override
    public void putStack(ItemStack stack) {
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return getStack();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getStack().getCount();
    }

    @Override
    public int getSlotStackLimit() {
        return getStack().getCount();
    }
}
