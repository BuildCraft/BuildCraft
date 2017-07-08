/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import buildcraft.lib.tile.item.IItemHandlerAdv;

public class SlotPhantom extends SlotBase implements IPhantomSlot {
    private final boolean canAdjustCount;

    public SlotPhantom(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY, boolean adjustableCount) {
        super(itemHandler, slotIndex, posX, posY);
        this.canAdjustCount = adjustableCount;
    }


    public SlotPhantom(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        this(itemHandler, slotIndex, posX, posY, true);
    }

    @Override
    public boolean canAdjustCount() {
        return canAdjustCount;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
