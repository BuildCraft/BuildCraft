/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;

public class ContainerRefinery extends BuildCraftContainer {
	
	TileRefinery refinery;
	
	public ContainerRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super (3);

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlot(new Slot(inventory, k1 + l * 9 + 9, 8 + k1 * 18,
						123 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlot(new Slot(inventory, i1, 8 + i1 * 18, 181));
		}
		
		this.refinery = refinery;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return refinery.isUseableByPlayer(entityplayer);
	}
	
	

}
