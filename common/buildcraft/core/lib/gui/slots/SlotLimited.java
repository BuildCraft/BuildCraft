/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui.slots;

import net.minecraft.inventory.IInventory;

public class SlotLimited extends SlotBase {

	private final int limit;

	public SlotLimited(IInventory iinventory, int slotIndex, int posX, int posY, int limit) {
		super(iinventory, slotIndex, posX, posY);
		this.limit = limit;
	}

	@Override
	public int getSlotStackLimit() {
		return limit;
	}


}
