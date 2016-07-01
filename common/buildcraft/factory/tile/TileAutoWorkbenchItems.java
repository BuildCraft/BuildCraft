/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase {
    public TileAutoWorkbenchItems() {
        super(9);
    }

    @Override
    protected WorkbenchCrafting createCrafting() {
        return new WorkbenchCraftingItems(3, 3);
    }

    public class WorkbenchCraftingItems extends WorkbenchCrafting {
        public WorkbenchCraftingItems(int width, int height) {
            super(width, height);
            for (int i = 0; i < this.craftingSlots.length; i++) {
                this.craftingSlots[i] = new CraftSlotItem(i);
            }
        }
    }
}
