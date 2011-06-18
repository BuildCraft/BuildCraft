package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

class CraftingTemplate extends Container {
	
	IInventory playerIInventory;
	TileTemplate template;
	
	public CraftingTemplate (IInventory playerInventory, TileTemplate template) {		
		this.playerIInventory = playerInventory;
		this.template = template;
		
		addSlot(new Slot(template, 0, 55, 35));
		addSlot(new Slot(template, 1, 114, 35));

        for(int l = 0; l < 3; l++)
        {
            for(int k1 = 0; k1 < 9; k1++)
            {
                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 84 + l * 18));
            }

        }

        for(int i1 = 0; i1 < 9; i1++)
        {
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
	}
	
	public ItemStack getStackInSlot(int i)
    {	
		if (i < template.getSizeInventory()) {
			return template.getStackInSlot(i);
		} else {
			return playerIInventory.getStackInSlot(i
					- template.getSizeInventory());
		}		
    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {		
		return isUsableByPlayer(entityplayer);
	}
	
}