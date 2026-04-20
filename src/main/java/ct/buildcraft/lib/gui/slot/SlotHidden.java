/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class SlotHidden extends Slot {

    private boolean active;

    public SlotHidden(Container inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    public void show() {
    	this.active = true;
    }

    public void hide() {
    	this.active = false;
    }

	@Override
	public boolean isActive() {
		return active;
	}
    
    
}
