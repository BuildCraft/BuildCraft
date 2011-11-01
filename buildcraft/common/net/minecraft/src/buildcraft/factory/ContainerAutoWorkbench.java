/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
