/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.silicon;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;
import net.minecraft.src.mod_BuildCraftSilicon;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketCoordinates;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;

class ContainerAssemblyTable extends BuildCraftContainer {

	IInventory playerIInventory;
	TileAssemblyTable table;

	int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
	boolean networkSynchronized = false;
	
	public ContainerAssemblyTable(IInventory playerInventory, TileAssemblyTable table) {
		super(table.getSizeInventory());
		this.playerIInventory = playerInventory;
		
		for (int l = 0; l < 4; l++) {
			for (int k1 = 0; k1 < 3; k1++) {
				addSlot(new Slot(table, k1 + l * 3, 8 + k1 * 18,
						36 + l * 18));
			}

		}
		

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18,
						123 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 181));
		}
		
		this.table = table;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return table.isUseableByPlayer(entityplayer);
	}
	
	@Override
	public void updateCraftingResults() {
		super.updateCraftingResults();
		
		for (int i = 0; i < crafters.size(); i++) {
			ICrafting crafting = (ICrafting) crafters.get(i);

			crafting.updateCraftingInventoryInfo(this, 0, table.xCoord);
			crafting.updateCraftingInventoryInfo(this, 1, table.yCoord);
			crafting.updateCraftingInventoryInfo(this, 2, table.zCoord);
		}
	}
	
	public void updateProgressBar(int i, int j) {	
		if (i == 0) {
			x = j;
		} else if (i == 1) {
			y = j;
		} else if (i == 2) {
			z = j;
		}
		
		if (!networkSynchronized && x != Integer.MAX_VALUE && y != Integer.MAX_VALUE && z != Integer.MAX_VALUE)
			networkSynchronized = true;		
			CoreProxy.sendToServer(new PacketCoordinates(PacketIds.SELECTION_ASSEMBLY_GET, x, y, z).getPacket());
	}
}