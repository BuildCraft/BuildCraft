package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;

class CraftingTemplate extends CraftingTemplateRoot {
	
	public CraftingTemplate (IInventory playerInventory, TileTemplate template) {
		super (playerInventory, template);
	}
	
	public void onCraftGuiOpened(ICrafting icrafting) {
        super.onCraftGuiOpened(icrafting);
        icrafting.updateCraftingInventoryInfo(this, 0, template.computingTime);
    }
	
	public void updateCraftingMatrix() {
        super.updateCraftingMatrix();
        for(int i = 0; i < crafters.size(); i++) {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(computingTime != template.computingTime) {
				icrafting.updateCraftingInventoryInfo(this, 0,
						template.computingTime);
            }
        }

        computingTime = template.computingTime;
    }
	
}