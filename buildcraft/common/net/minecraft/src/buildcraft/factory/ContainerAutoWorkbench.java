/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.World;

public class ContainerAutoWorkbench extends ContainerWorkbench {

	TileAutoWorkbench tile;
	
	public ContainerAutoWorkbench(InventoryPlayer inventoryplayer, World world,
			TileAutoWorkbench tile) {
		super(inventoryplayer, world, 0, 0, 0);
					
		this.tile = tile;
		
		for (int l = 0; l < craftMatrix.getSizeInventory(); ++l) {		
			craftMatrix.setInventorySlotContents(l, tile.getStackInSlot(l));
		}
		
		onCraftMatrixChanged (craftMatrix);
	}
	
	public void onCraftGuiClosed(EntityPlayer entityplayer)
    {
		InventoryPlayer inventoryplayer = entityplayer.inventory;
        if(inventoryplayer.getItemStack() != null)
        {
            entityplayer.dropPlayerItem(inventoryplayer.getItemStack());
            inventoryplayer.setItemStack(null);
        }
        
		for (int l = 0; l < craftMatrix.getSizeInventory(); ++l) {		
			tile.setInventorySlotContents(l, craftMatrix.getStackInSlot(l));
		}

    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}
	
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
