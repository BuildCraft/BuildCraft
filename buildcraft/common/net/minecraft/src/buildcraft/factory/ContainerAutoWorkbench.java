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
