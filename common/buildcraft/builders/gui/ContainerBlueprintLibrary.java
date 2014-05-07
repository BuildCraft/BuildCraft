/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotBlueprintLibrary;
import buildcraft.core.gui.slots.SlotOutput;

public class ContainerBlueprintLibrary extends BuildCraftContainer {

	protected IInventory playerInventory;
	protected TileBlueprintLibrary library;

	private int progressIn, progressOut;

	public ContainerBlueprintLibrary(EntityPlayer player, TileBlueprintLibrary library) {
		super(library.getSizeInventory());
		this.playerInventory = player.inventory;
		this.library = library;

		addSlotToContainer(new SlotBlueprintLibrary(library, player, 0, 211, 61));
		addSlotToContainer(new SlotOutput(library, 1, 167, 61));

		addSlotToContainer(new SlotBlueprintLibrary(library, player, 2, 167, 79));
		addSlotToContainer(new SlotOutput(library, 3, 211, 79));

		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 66 + k1 * 18, 140 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 66 + i1 * 18, 198));
		}
	}

	@Override
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player) {
		// When downloading or uploading a blueprint, the server needs to know
		// who requested it. The way to do it so far is by recording the last
		// player that clicks on the slots. To be improved if the method is
		// not robust enough (e.g. what if the player is not logged anymore?
		// is that robust against race conditions? etc.)

		if (slotNum == 0) {
			library.uploadingPlayer = player;
		} else if (slotNum == 2) {
			library.downloadingPlayer = player;
		}

		return super.slotClick(slotNum, mouseButton, modifier, player);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++) {
			ICrafting icrafting = (ICrafting) crafters.get(i);
			if (progressIn != library.progressIn) {
				icrafting.sendProgressBarUpdate(this, 0, library.progressIn);
			}
			if (progressOut != library.progressOut) {
				icrafting.sendProgressBarUpdate(this, 1, library.progressOut);
			}
		}

		progressIn = library.progressIn;
		progressOut = library.progressOut;
	}

	@Override
	public void updateProgressBar(int i, int j) {
		if (i == 0) {
			library.progressIn = j;
		} else if (i == 1) {
			library.progressOut = j;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return library.isUseableByPlayer(entityplayer);
	}
}
