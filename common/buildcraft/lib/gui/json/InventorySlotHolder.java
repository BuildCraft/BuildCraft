/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.tile.item.IItemHandlerAdv;

// STUB(R.Chen): Forge SlotItemHandler.getItemHandler() → SlotBase.itemHandler; IItemHandler → IItemHandlerAdv stub.
public class InventorySlotHolder {

    public final Slot[] slots;

    public InventorySlotHolder(ScreenHandler container, Inventory inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.slots) {
            if (s.inventory == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }

    public InventorySlotHolder(ScreenHandler container, IItemHandlerAdv inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.slots) {
            if (s instanceof SlotBase && ((SlotBase) s).itemHandler == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }
}
