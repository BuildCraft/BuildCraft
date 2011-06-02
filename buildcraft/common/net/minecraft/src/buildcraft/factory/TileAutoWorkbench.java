package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.StackUtil;

public class TileAutoWorkbench extends TileEntity implements
		ISpecialInventory {

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

		ItemStack newStack = stackList[i].copy();
		newStack.stackSize = j;
		
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

	@Override
	public boolean addItemFromPipe(ItemStack stack, boolean doAdd, Orientations from) {
		StackUtil stackUtils = new StackUtil(stack);
		
		int minSimilar = Integer.MAX_VALUE;
		int minSlot = -1;
		
		for (int j = 0; j < getSizeInventory(); ++j) {
			ItemStack stackItem = getStackInSlot(j);
			
			if (stackItem != null && stackItem.stackSize > 0
					&& stackItem.itemID == stackItem.itemID
					&& stackItem.getItemDamage() == stackItem.getItemDamage()
					&& stackItem.stackSize < minSimilar) {
				minSimilar = stackItem.stackSize;
				minSlot = j;
			}								
		}
		
		if (minSlot != -1) {
			if (stackUtils.tryAdding(this, minSlot, doAdd, false)) {			
				if (doAdd && stack.stackSize != 0) {
					addItemFromPipe(stack, doAdd, from);
				}
				
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public ItemStack extractItemToPipe(boolean doRemove, Orientations from) {
		InventoryCrafting craftMatrix = new InventoryCrafting(new Container () {
			@SuppressWarnings("all")
			public boolean isUsableByPlayer(EntityPlayer entityplayer) {
				return false;
			}

			@SuppressWarnings("all")
			public boolean canInteractWith(EntityPlayer entityplayer) {
				// TODO Auto-generated method stub
				return false;
			}}, 3, 3);	

		for (int i = 0; i < getSizeInventory(); ++i) {
			ItemStack stack = getStackInSlot(i);

			if (stack != null && stack.stackSize == 1) {
				return null;
			}

			craftMatrix.setInventorySlotContents(i, stack);
		}

		ItemStack resultStack = CraftingManager.getInstance().findMatchingRecipe(
				craftMatrix);

		if (resultStack != null && doRemove) {
			for (int i = 0; i < getSizeInventory(); ++i) {
				ItemStack stack = getStackInSlot(i);

				if (stack != null) {
					decrStackSize(i, 1);
				}
			}
		}

		return resultStack;
	}
}
