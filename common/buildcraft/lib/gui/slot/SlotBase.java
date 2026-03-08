/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotBase extends SlotItemHandler {
    public final int handlerIndex;
    public final IItemHandlerAdv itemHandler;

    public SlotBase(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
        this.handlerIndex = slotIndex;
        this.itemHandler = itemHandler;
    }

    public boolean canShift() {
        return true;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return itemHandler.canSet(handlerIndex, stack);
    }

    /** @param stack
     * @param simulate
     * @return The left over. */
    public ItemStack insert(ItemStack stack, boolean simulate) {
        return getItemHandler().insertItem(handlerIndex, stack, simulate);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (itemHandler instanceof ItemHandlerSimple) {
            ((ItemHandlerSimple) itemHandler).setStackInSlot(handlerIndex, getItem());
        }
    }
}
