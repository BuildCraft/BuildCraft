package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

class CraftingBuilder extends Container {
	
	IInventory playerIInventory;
	IInventory builderInventory;
	
	public CraftingBuilder (IInventory playerInventory, IInventory builderInventory) {		
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
	
	public ItemStack getStackInSlot(int i)
    {	
		if (i < builderInventory.getSizeInventory()) {
			return builderInventory.getStackInSlot(i);
		} else {
			return playerIInventory.getStackInSlot(i
					- builderInventory.getSizeInventory());
		}		
    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {		
		return isUsableByPlayer(entityplayer);
	}
	
}