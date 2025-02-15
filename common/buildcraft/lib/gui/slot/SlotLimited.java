/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;

public class SlotLimited extends SlotBase {

    private final int limit;

    public SlotLimited(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY, int limit) {
        super(itemHandler, slotIndex, posX, posY);
        this.limit = limit;
    }

    @Override
//    public int getSlotStackLimit()
    public int getMaxStackSize() {
        return limit;
    }
}
