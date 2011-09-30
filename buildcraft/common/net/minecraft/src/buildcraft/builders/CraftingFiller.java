/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;

class CraftingFiller extends BuildCraftContainer {
	
	IInventory playerIInventory;
	IInventory fillerInventory;
	
	public CraftingFiller (IInventory playerInventory, IInventory fillerInventory) {	
		super (fillerInventory.getSizeInventory() + 9);
		this.playerIInventory = playerInventory;
		this.fillerInventory = fillerInventory;
		
		for(int k = 0; k < 3; k++) {
            for(int j1 = 0; j1 < 3; j1++) {
                addSlot(new Slot(fillerInventory, j1 + k * 3, 31 + j1 * 18, 16 + k * 18));
            }
        }
		
		for(int k = 0; k < 3; k++) {
            for(int j1 = 0; j1 < 9; j1++) {
                addSlot(new Slot(fillerInventory, 9 + j1 + k * 9, 8 + j1 * 18, 85 + k * 18));
            }
        }

        for(int l = 0; l < 3; l++)
        {
            for(int k1 = 0; k1 < 9; k1++)
            {
                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 153 + l * 18));
            }

        }

        for(int i1 = 0; i1 < 9; i1++)
        {
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 211));
        }
	}
	
}