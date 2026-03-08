/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.IntFunction;

public class SlotDisplay extends Slot {
    // private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private static IInventory emptyInventory = new Inventory(0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
//    public ItemStack onTake(PlayerEntity player, ItemStack stack)
    public ItemStack onTake(PlayerEntity player, ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
//    public boolean isItemValid(ItemStack stack)
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
//    public ItemStack getStack()
    public ItemStack getItem() {
        return getter.apply(getSlotIndex()).copy();
    }

    @Override
//    public void putStack(ItemStack stack)
    public void set(ItemStack stack) {
    }

    @Override
//    public boolean canTakeStack(PlayerEntity player)
    public boolean mayPickup(PlayerEntity player) {
        return false;
    }

    @Override
//    public ItemStack decrStackSize(int amount)
    public ItemStack remove(int amount) {
//        return getStack();
        return getItem();
    }

    @Override
//    public int getItemStackLimit(ItemStack stack)
    public int getMaxStackSize(ItemStack stack) {
//        return getStack().getCount();
        return getItem().getCount();
    }

    @Override
//    public int getSlotStackLimit()
    public int getMaxStackSize() {
//        return getStack().getCount();
        return getItem().getCount();
    }
}
