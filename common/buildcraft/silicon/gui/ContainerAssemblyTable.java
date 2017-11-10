/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.silicon.TileAssemblyTable;

public class ContainerAssemblyTable extends BuildCraftContainer {
	IInventory playerIInventory;
	TileAssemblyTable table;

	public ContainerAssemblyTable(IInventory playerInventory, TileAssemblyTable table) {
		super(table.getSizeInventory());
		this.playerIInventory = playerInventory;

		for (int l = 0; l < 4; l++) {
			for (int k1 = 0; k1 < 3; k1++) {
				addSlotToContainer(new Slot(table, k1 + l * 3, 8 + k1 * 18, 36 + l * 18));
			}

		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 123 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 181));
		}

		this.table = table;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return table.isUseableByPlayer(entityplayer);
	}

	@Override
	public void updateProgressBar(int i, int j) {
		table.getGUINetworkData(i, j);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (Object crafter : crafters) {
			table.sendGUINetworkData(this, (ICrafting) crafter);
		}
	}
}
