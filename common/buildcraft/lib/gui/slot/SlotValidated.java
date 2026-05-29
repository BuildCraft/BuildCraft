/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotValidated extends Slot {

    public SlotValidated(Inventory inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    public boolean canInsert(ItemStack itemStack) {
        return inventory.isValid(getIndex(), itemStack);
    }
}
