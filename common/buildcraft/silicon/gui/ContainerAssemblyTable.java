/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon.gui;

import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

public class ContainerAssemblyTable extends BuildCraftContainer {

	IInventory playerIInventory;
	TileAssemblyTable table;

	int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
	boolean networkSynchronized = false;

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

	// @Override client side only
	public void updateProgressBar(int i, int j) {
		table.getGUINetworkData(i, j);
	}

	@Override
	public void updateCraftingResults() {
		super.updateCraftingResults();

		for (int i = 0; i < crafters.size(); i++)
			table.sendGUINetworkData(this, (ICrafting) crafters.get(i));
	}
}