/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.slot;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
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
    public boolean canTakeItems(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean canInsert(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
