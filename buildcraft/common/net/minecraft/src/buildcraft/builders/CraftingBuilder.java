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

class CraftingBuilder extends BuildCraftContainer {
	
	IInventory playerIInventory;
	IInventory builderInventory;
	
	public CraftingBuilder (IInventory playerInventory, IInventory builderInventory) {		
		super (builderInventory.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.builderInventory = builderInventory;
		
		addSlot(new Slot(builderInventory, 0, 80, 27));
		
		for(int k = 0; k < 3; k++) {
            for(int j1 = 0; j1 < 9; j1++) {
                addSlot(new Slot(builderInventory, 1 + j1 + k * 9, 8 + j1 * 18, 72 + k * 18));
            }

        }

        for(int l = 0; l < 3; l++)
        {
            for(int k1 = 0; k1 < 9; k1++)
            {
                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
            }

        }

        for(int i1 = 0; i1 < 9; i1++)
        {
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }
	}	   
	
}