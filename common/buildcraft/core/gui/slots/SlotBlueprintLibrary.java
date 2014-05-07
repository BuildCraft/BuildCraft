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

import buildcraft.builders.TileBlueprintLibrary;

public class SlotBlueprintLibrary extends SlotBase {
	private TileBlueprintLibrary library;
	private EntityPlayer player;
	private int slot;

	public SlotBlueprintLibrary(IInventory iinventory, EntityPlayer player, int slotIndex, int posX, int posY) {
		super(iinventory, slotIndex, posX, posY);
		this.library = (TileBlueprintLibrary) iinventory;
		this.slot = slotIndex;
		this.player = player;
	}

	public void onSlotChanged() {
		if (slot == 0) {
			library.uploadingPlayer = player;
		} else if (slot == 2) {
			library.downloadingPlayer = player;
		}

		this.inventory.markDirty();
	}
}