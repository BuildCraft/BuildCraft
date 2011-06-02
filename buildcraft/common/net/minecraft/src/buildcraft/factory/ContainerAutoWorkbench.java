package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.World;

public class ContainerAutoWorkbench extends ContainerWorkbench {

	public int i, j, k;
	public World world;
	
	public ContainerAutoWorkbench(InventoryPlayer inventoryplayer, World world,
			int i, int j, int k) {
		super(inventoryplayer, world, i, j, k);
		
		this.i = i;
		this.j = j;
		this.k = k;
		this.world = world;
		
		TileAutoWorkbench tile = (TileAutoWorkbench) world.getBlockTileEntity(
				i, j, k);
		
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
        
    	TileAutoWorkbench tile = (TileAutoWorkbench) world.getBlockTileEntity(
				i, j, k);
        
		for (int l = 0; l < craftMatrix.getSizeInventory(); ++l) {		
			tile.setInventorySlotContents(l, craftMatrix.getStackInSlot(l));
		}

    }
	
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		if (world.getBlockId(i, j, k) != BuildCraftFactory.autoWorkbenchBlock.blockID) {
			return false;
		}
		return entityplayer.getDistanceSq((double) i + 0.5D, (double) j + 0.5D,
				(double) k + 0.5D) <= 64D;
	}

}
