/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

// public class SlotValidated extends Slot
public class SlotValidated extends SlotBase {

    // public SlotValidated(Container inv, int id, int x, int y)
    public SlotValidated(IItemHandlerAdv inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    // public boolean isItemValid(ItemStack itemStack)
    public boolean mayPlace(@Nonnull ItemStack itemStack) {
        // return inventory.isItemValidForSlot(this.getSlotIndex(), itemStack);
        return itemHandler.canSet(handlerIndex, itemStack);
    }
}
