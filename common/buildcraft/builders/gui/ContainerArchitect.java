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
import buildcraft.builders.TileArchitect;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotOutput;

public class ContainerArchitect extends BuildCraftContainer {

	protected IInventory playerIInventory;
	protected TileArchitect architect;
	protected int computingTime = 0;

	public ContainerArchitect(IInventory playerInventory, TileArchitect template) {
		super(template.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.architect = template;

		addSlotToContainer(new Slot(template, 0, 135, 35));
		addSlotToContainer(new SlotOutput(template, 1, 194, 35));

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 88 + k1 * 18, 84 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 88 + i1 * 18, 142));
		}
	}

	// FIXME: This is not called anymore

	@Override
	public void addCraftingToCrafters(ICrafting icrafting) {
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, architect.computingTime);
	}


	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < crafters.size(); i++) {
			ICrafting icrafting = (ICrafting) crafters.get(i);
			if (computingTime != architect.computingTime) {
				icrafting.sendProgressBarUpdate(this, 0, architect.computingTime);
			}
		}

		computingTime = architect.computingTime;
	}

	@Override
	public void updateProgressBar(int i, int j) {
		if (i == 0) {
			architect.computingTime = j;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return architect.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player) {
		if (slotNum == 0) {
			architect.currentAuthorName = player.getDisplayName();
		}

		return super.slotClick(slotNum, mouseButton, modifier, player);
	}

}
