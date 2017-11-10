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
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.builders.TileArchitect;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.gui.slots.SlotOutput;

public class ContainerArchitect extends BuildCraftContainer {

	protected IInventory playerIInventory;
	protected TileArchitect architect;
	protected int computingTime = 0;

	public ContainerArchitect(EntityPlayer player, TileArchitect template) {
		super(template.getSizeInventory());
		this.playerIInventory = player.inventory;
		this.architect = template;

		addSlotToContainer(new SlotArchitect(template, player, 0, 135, 35));
		addSlotToContainer(new SlotOutput(template, 1, 194, 35));

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(player.inventory, k1 + l * 9 + 9, 88 + k1 * 18, 84 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(player.inventory, i1, 88 + i1 * 18, 142));
		}
	}

	// FIXME: This is not called anymore

	@Override
	public void addCraftingToCrafters(ICrafting icrafting) {
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, architect.getComputingProgressScaled(24));
	}


	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (Object crafter : crafters) {
			ICrafting icrafting = (ICrafting) crafter;
			if (computingTime != architect.getComputingProgressScaled(24)) {
				icrafting.sendProgressBarUpdate(this, 0, architect.getComputingProgressScaled(24));
			}
		}

		computingTime = architect.getComputingProgressScaled(24);
	}

	@Override
	public void updateProgressBar(int i, int j) {
		if (i == 0) {
			computingTime = j;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return architect.isUseableByPlayer(entityplayer);
	}
}
