package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

class CraftingInv extends Container {
	
	IInventory playerIInventory;
	IInventory filterIInventory;
	
	public CraftingInv (IInventory playerInventory, IInventory filterInventory) {
		System.out.println ("CREATE CONTAINER");
		
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
            	System.out.println ("ADD SLOT: " + (k1 + l * 9 + 9));
                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
            }

        }

        for(int i1 = 0; i1 < 9; i1++)
        {
        	System.out.println ("ADD SLOT: " + i1);
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
        }
	}
	
//	public Slot func_20127_a(IInventory iinventory, int i) {
//		return getStackInSlot (i);
//	}
	
//    public Slot getSlot(int i)
//    {
//    	if (i < filterIInventory.getSizeInventory()) {
//			return filterIInventory.get(i);
//		} else {
//			return playerIInventory.getStackInSlot(i
//					- filterIInventory.getSizeInventory());
//		}
//    }
	
	public ItemStack getStackInSlot(int i)
    {
		System.out.println ("GET STACK IN SLOT");
		
		if (i < filterIInventory.getSizeInventory()) {
			return filterIInventory.getStackInSlot(i);
		} else {
			return playerIInventory.getStackInSlot(i
					- filterIInventory.getSizeInventory());
		}
		
//        Slot slot = (Slot)slots.get(i);
//        if(slot != null)
//        {
//            return slot.getStack();
//        } else
//        {
//            return null;
//        }
    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {		
		return isUsableByPlayer(entityplayer);
	}
	
}