/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import buildcraft.builders.TileArchitect;

public class SlotArchitect extends SlotBase {
	private TileArchitect architect;
	private EntityPlayer player;
	private int slot;

	public SlotArchitect(IInventory iinventory, EntityPlayer player, int slotIndex, int posX, int posY) {
		super(iinventory, slotIndex, posX, posY);
		this.architect = (TileArchitect) iinventory;
		this.slot = slotIndex;
		this.player = player;
	}

	@Override
	public void onSlotChanged() {
		if (slot == 0) {
			architect.currentAuthorName = player.getDisplayNameString();
		}

		this.inventory.markDirty();
	}
}