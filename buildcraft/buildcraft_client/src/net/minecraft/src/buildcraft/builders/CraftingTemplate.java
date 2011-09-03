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