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
    public boolean mayPickup(Player par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
