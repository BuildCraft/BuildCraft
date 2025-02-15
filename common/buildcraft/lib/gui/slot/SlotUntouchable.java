/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotUntouchable extends SlotBase implements IPhantomSlot {

    public SlotUntouchable(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
    }

    @Override
//    public boolean isItemValid(@Nonnull ItemStack itemstack)
    public boolean mayPlace(@Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
//    public boolean canTakeStack(Player par1EntityPlayer)
    public boolean mayPickup(Player par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean canAdjustCount() {
        return false;
    }

    @Override
    public boolean canShift() {
        return false;
    }
}
