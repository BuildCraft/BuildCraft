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
import net.minecraft.inventory.Slot;

public class SlotHidden extends Slot {

	private int saveX;
	private int saveY;

	public SlotHidden(IInventory inv, int index, int x, int y) {
		super(inv, index, x, y);

		saveX = x;
		saveY = y;
	}

	public void show() {
		xDisplayPosition = saveX;
		yDisplayPosition = saveY;
	}

	public void hide() {
		xDisplayPosition = 9999;
		yDisplayPosition = 9999;
	}
}