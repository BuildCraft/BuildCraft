/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.lib.gui.slots.SlotBase;

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

	@Override
	public void onSlotChanged() {
		// When downloading or uploading a blueprint, the server needs to know
		// who requested it. The way to do it so far is by recording the last
		// player that clicks on the slots. To be improved if the method is
		// not robust enough (e.g. what if the player is not logged anymore?
		// is that robust against race conditions? etc.)

		if (slot == 0) {
			library.uploadingPlayer = player;
		} else if (slot == 2) {
			library.downloadingPlayer = player;
		}

		this.inventory.markDirty();
	}
}