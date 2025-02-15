/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntFunction;

public class SlotDisplay extends Slot {
    // private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private static Container emptyInventory = new SimpleContainer(0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
//    public ItemStack onTake(Player player, ItemStack stack)
    public void onTake(Player player, ItemStack stack) {
//        return ItemStack.EMPTY;
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
//    public boolean canTakeStack(Player player)
    public boolean mayPickup(Player player) {
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
