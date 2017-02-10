/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.slot;

import buildcraft.lib.tile.item.IItemHandlerAdv;

public class SlotLimited extends SlotBase {

    private final int limit;

    public SlotLimited(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY, int limit) {
        super(itemHandler, slotIndex, posX, posY);
        this.limit = limit;
    }

    @Override
    public int getSlotStackLimit() {
        return limit;
    }
}
