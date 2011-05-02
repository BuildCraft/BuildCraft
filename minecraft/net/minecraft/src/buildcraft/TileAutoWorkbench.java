package net.minecraft.src.buildcraft;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.TilePipe.EntityData;

public class TileAutoWorkbench extends TileEntity implements IInventory {

	private ItemStack stackList[];
	
	public TileAutoWorkbench () {
		stackList = new ItemStack [3*3];
	}
	
	@Override
	public int getSizeInventory() {

		return stackList.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {

		return stackList [i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {

		ItemStack newStack = new ItemStack(stackList[i].getItem(), j);
		
		stackList [i].stackSize -= j;
		
		if (stackList[i].stackSize == 0) {
			stackList[i] = null;
		}
		
		return newStack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		stackList [i] = itemstack;
		
	}

	@Override
	public String getInvName() {
	
		return "Crafting Container";
	}

	@Override
	public int getInventoryStackLimit() {

		return 64;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {

		return true;
	}
	
	 public void readFromNBT(NBTTagCompound nbttagcompound) {
	     super.readFromNBT(nbttagcompound);
	     
	     NBTTagList nbttaglist = nbttagcompound.getTagList("stackList");
	     
	     stackList = new ItemStack [nbttaglist.tagCount()];
	     
	     for (int i = 0; i < stackList.length; ++i) {  
	    	 NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist
				.tagAt(i);	
	    	 
	    	 if (!nbttagcompound2.getBoolean("isNull")) {
	    		 stackList [i] = new ItemStack(nbttagcompound2);
	    	 }
	     }
	 }

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
    	NBTTagList nbttaglist = new NBTTagList();
    	
    	for (int i = 0; i < stackList.length; ++i) {    		
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.setTag(nbttagcompound2);
    		if (stackList [i] == null) {
    			nbttagcompound2.setBoolean("isNull", true);
    		} else {
    			nbttagcompound2.setBoolean("isNull", false);
    			stackList [i].writeToNBT(nbttagcompound2);
    		}
    		
    	}
    	
    	nbttagcompound.setTag("stackList", nbttaglist);
	}
}
