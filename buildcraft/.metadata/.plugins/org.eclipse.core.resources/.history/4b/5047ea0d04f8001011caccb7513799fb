/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;

class CraftingTemplate extends CraftingTemplateRoot {
	
	public CraftingTemplate (IInventory playerInventory, TileTemplate template) {
		super (playerInventory, template);
	}
	
	public void updateCraftingResults() {
        super.updateCraftingResults();
        for(int i = 0; i < crafters.size(); i++)
        {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(computingTime != template.computingTime) {
                icrafting.updateCraftingInventoryInfo(this, 0, template.computingTime);
            }
        }

        computingTime = template.computingTime;  
    }

	@Override
	public void func_20112_a(int i, int j) {		
		if (i == 0) {
			template.computingTime = j;
		}
	}
}