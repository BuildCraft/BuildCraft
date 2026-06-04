/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

// STUB(R.Chen): Forge xPos/yPos fields are final in Yarn — hide/show via isEnabled() instead.
public class SlotHidden extends Slot {

    private boolean hidden = false;

    public SlotHidden(Inventory inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    public void show() {
        hidden = false;
    }

    public void hide() {
        hidden = true;
    }

    @Override
    public boolean isEnabled() {
        return !hidden;
    }
}
