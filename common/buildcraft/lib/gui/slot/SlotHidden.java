/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class SlotHidden extends Slot {

    private int saveX;
    private int saveY;

    public SlotHidden(IInventory inv, int index, int x, int y) {
        super(inv, index, x, y);

        saveX = x;
        saveY = y;
    }

    public void show() {
//        xPos = saveX;
        x = saveX;
//        yPos = saveY;
        y = saveY;
    }

    public void hide() {
//        xPos = 9999;
        x = 9999;
//        yPos = 9999;
        y = 9999;
    }
}
