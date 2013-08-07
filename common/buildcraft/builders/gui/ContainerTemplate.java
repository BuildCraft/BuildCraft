/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import buildcraft.builders.TileArchitect;
import buildcraft.core.gui.BuildCraftContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerTemplate extends BuildCraftContainer {

	protected IInventory playerIInventory;
	protected TileArchitect template;
	protected int computingTime = 0;

	public ContainerTemplate(IInventory playerInventory, TileArchitect template) {
		super(template.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.template = template;

		addSlotToContainer(new Slot(template, 0, 55, 35));
		addSlotToContainer(new Slot(template, 1, 114, 35));

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 84 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}
	}

	// FIXME: This is not called anymore
	/*
	 * @Override public void onCraftGuiOpened(ICrafting icrafting) { super.onCraftGuiOpened(icrafting); icrafting.updateCraftingInventoryInfo(this, 0,
	 * template.computingTime); }
	 */

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++) {
			ICrafting icrafting = (ICrafting) crafters.get(i);
			if (computingTime != template.computingTime) {
				icrafting.sendProgressBarUpdate(this, 0, template.computingTime);
			}
		}

		computingTime = template.computingTime;
	}

	@Override
	public void updateProgressBar(int i, int j) {
		if (i == 0) {
			template.computingTime = j;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return template.isUseableByPlayer(entityplayer);
	}

}
