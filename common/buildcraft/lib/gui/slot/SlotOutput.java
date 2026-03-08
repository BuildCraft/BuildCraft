/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotOutput extends SlotBase {

    public SlotOutput(IItemHandlerAdv handler, int slotIndex, int posX, int posY) {
        super(handler, slotIndex, posX, posY);
    }

    @Override
//    public boolean isItemValid(@Nonnull ItemStack itemstack)
    public boolean mayPlace(@Nonnull ItemStack itemstack) {
        return false;
    }
}
