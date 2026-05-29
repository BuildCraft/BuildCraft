/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.slot;

import java.util.function.IntFunction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotDisplay extends Slot {
    private static final SimpleInventory EMPTY_INVENTORY = new SimpleInventory(0);
    private final IntFunction<ItemStack> getter;

    public SlotDisplay(IntFunction<ItemStack> getter, int index, int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, index, xPosition, yPosition);
        this.getter = getter;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return getter.apply(getIndex()).copy();
    }

    @Override
    public void setStack(ItemStack stack) {
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return getStack();
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return getStack().getCount();
    }

    @Override
    public int getMaxItemCount() {
        return getStack().getCount();
    }
}
