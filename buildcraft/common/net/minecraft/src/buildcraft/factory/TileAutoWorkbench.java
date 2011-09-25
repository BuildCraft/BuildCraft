package net.minecraft.src.buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.Utils;

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
	    		 stackList [i] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
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
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		StackUtil stackUtils = new StackUtil(stack);
		
		int minSimilar = Integer.MAX_VALUE;
		int minSlot = -1;
		
		for (int j = 0; j < getSizeInventory(); ++j) {
			ItemStack stackInInventory = getStackInSlot(j);
			
			if (stackInInventory != null && stackInInventory.stackSize > 0
					&& stackInInventory.itemID == stack.itemID
					&& stackInInventory.getItemDamage() == stack.getItemDamage()
					&& stackInInventory.stackSize < minSimilar) {
				minSimilar = stackInInventory.stackSize;
				minSlot = j;
			}								
		}
		
		if (minSlot != -1) {
			if (stackUtils.tryAdding(this, minSlot, doAdd, false)) {			
				if (doAdd && stack.stackSize != 0) {
					addItem(stack, doAdd, from);
				}
				
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	class StackPointer {
		IInventory inventory;
		int index;
		ItemStack item;
	}
	
	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
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

		LinkedList<StackPointer> pointerList = new LinkedList<StackPointer>();
		
		for (int i = 0; i < getSizeInventory(); ++i) {
			ItemStack stack = getStackInSlot(i);
			
			if (stack != null) {				
				if (stack.stackSize <= 1) {
					StackPointer pointer = getNearbyItem(stack.itemID,
							stack.getItemDamage());

					if (pointer == null) {
						resetPointers(pointerList);

						return null;
					} else {
						pointerList.add(pointer);
					}					
				} else {
					StackPointer pointer = new StackPointer();
					pointer.inventory = this;
					pointer.item = this.decrStackSize(i, 1);
					pointer.index = i;
					
					pointerList.add(pointer);
				}
			}

			craftMatrix.setInventorySlotContents(i, stack);
		}
		
		ItemStack resultStack = CraftingManager.getInstance().findMatchingRecipe(
				craftMatrix);

		if (resultStack == null || !doRemove) {
			resetPointers(pointerList);
		} else {
			for (StackPointer p : pointerList) {
				// replace with the container where appropriate
				
				if (p.item.getItem().getContainerItem() != null) {
					ItemStack newStack = new ItemStack(p.item.getItem()
							.getContainerItem(), 1);

					p.inventory.setInventorySlotContents(p.index, newStack);					
				}
			}
		}

		return resultStack;
	}
	
	public void resetPointers (LinkedList <StackPointer> pointers) {
		for (StackPointer p : pointers) {
			ItemStack item = p.inventory.getStackInSlot(p.index);
			
			if (item == null) {
				p.inventory.setInventorySlotContents(p.index, p.item);
			} else {
				p.inventory.getStackInSlot(p.index).stackSize++;
			}
		}
	}
	
	public StackPointer getNearbyItem (int itemId, int damage) {
		StackPointer pointer = null;

		pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.XNeg);
		
		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.XPos);	
		}
		
		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.YNeg);	
		}
		
		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.YPos);	
		}
		
		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.ZNeg);	
		}
		
		if (pointer == null) {
			pointer = getNearbyItemFromOrientation(itemId, damage, Orientations.ZPos);	
		}
		
		return pointer;
	}
	
	public StackPointer getNearbyItemFromOrientation (int itemId, int damage, Orientations orientation) {
		Position p = new Position (xCoord, yCoord, zCoord, orientation);
		p.moveForwards(1.0);
		
		TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
				(int) p.z);
		
		if (tile instanceof ISpecialInventory) {
			// Don't get stuff out of ISpecialInventory for now / we wouldn't
			// know how to put it back... And it's not clear if we want to
			// have workbenches automatically getting things from one another.
		} else if (tile instanceof IInventory) {
			IInventory inventory = Utils.getInventory((IInventory) tile);
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				ItemStack stack = inventory.getStackInSlot(j);
				
				if (stack != null && stack.stackSize > 0
						&& stack.itemID == itemId
						&& stack.getItemDamage() == damage) {
					inventory.decrStackSize(j, 1);
					
					StackPointer result = new StackPointer();
					result.inventory = inventory;
					result.index = j;
					result.item = stack;					
					
					return result;
				}
			}
		}		
		
		return null;
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
}
