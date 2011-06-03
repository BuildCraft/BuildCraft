package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

class CraftingDiamondPipe extends Container {
	
	IInventory playerIInventory;
	IInventory filterIInventory;
	
	public CraftingDiamondPipe (IInventory playerInventory, IInventory filterInventory) {		
		this.playerIInventory = playerInventory;
		this.filterIInventory = filterInventory;
		
		for(int k = 0; k < 6; k++)
        {
            for(int j1 = 0; j1 < 9; j1++)
            {
                addSlot(new Slot(filterInventory, j1 + k * 9, 8 + j1 * 18, 18 + k * 18));
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
		if (i < filterIInventory.getSizeInventory()) {
			return filterIInventory.getStackInSlot(i);
		} else {
			return playerIInventory.getStackInSlot(i
					- filterIInventory.getSizeInventory());
		}		
    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {		
		return isUsableByPlayer(entityplayer);
	}
	
}