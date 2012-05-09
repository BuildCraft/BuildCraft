/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;

class CraftingTemplate extends CraftingTemplateRoot {

	public CraftingTemplate (IInventory playerInventory, TileArchitect template) {
		super (playerInventory, template);
	}

	@Override
	public void updateCraftingResults() {
        super.updateCraftingResults();
        for(int i = 0; i < crafters.size(); i++)
        {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(computingTime != template.computingTime)
				icrafting.updateCraftingInventoryInfo(this, 0, template.computingTime);
        }

        computingTime = template.computingTime;
    }

	@Override
	public void updateProgressBar(int i, int j) {
		if (i == 0)
			template.computingTime = j;
	}
}