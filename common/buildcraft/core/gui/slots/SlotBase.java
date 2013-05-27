/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui.slots;

import buildcraft.core.gui.tooltips.ToolTip;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotBase extends Slot {

	private ToolTip toolTips;

	public SlotBase(IInventory iinventory, int slotIndex, int posX, int posY) {
		super(iinventory, slotIndex, posX, posY);
	}

	public boolean canShift() {
		return true;
	}

	/**
	 * @return the toolTips
	 */
	public ToolTip getToolTip() {
		return toolTips;
	}

	/**
	 * @param toolTips the tooltips to set
	 */
	public void setToolTips(ToolTip toolTips) {
		this.toolTips = toolTips;
	}
}
