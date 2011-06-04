package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class TileDiamondPipe extends TilePipe implements IInventory,
		ISpecialInventory {
	
	ItemStack [] items = new ItemStack [54];
	
	public TileDiamondPipe () {
		items = new ItemStack [getSizeInventory()];
	}
	
	@Override
	public int getSizeInventory() { 
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items [i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {		
		ItemStack stack = items [i].copy();
		stack.stackSize = j;
		
		items [i].stackSize -= j;
		
		if (items [i].stackSize == 0) {
			items [i] = null;
		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
	}

	@Override
	public String getInvName() {		
		return "Filters";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public LinkedList<Orientations> getPossibleMovements(Position pos,
			EntityPassiveItem item) {
		LinkedList<Orientations> filteredOrientations = new LinkedList<Orientations>();
		LinkedList<Orientations> defaultOrientations = new LinkedList<Orientations>();
		
		LinkedList<Orientations> possibilities = super.getPossibleMovements(new Position(
				xCoord, yCoord, zCoord, pos.orientation), item);
								
		//Filtered outputs
		for (Orientations dir : possibilities) {
			boolean foundFilter = false;

			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = getStackInSlot(dir.ordinal() * 9 + slot);

				if (stack != null) {
					foundFilter = true;
				}

				if (stack != null
						&& stack.itemID == item.item.itemID
						&& stack.getItemDamage() == item.item
								.getItemDamage()) {
					
					// NB: if there's several of the same match, the probability
					// to use that filter is higher, this is why there's no
					// break here.
					filteredOrientations.add(dir);
				} 
			}
			if (!foundFilter) {				
				defaultOrientations.add(dir);
			}
		}
		if(filteredOrientations.size() != 0) {
			System.out.println ("FILTER A" + filteredOrientations.size());
			return filteredOrientations;
		} else {
			System.out.println ("FILTER B ");			
			return defaultOrientations;
		}
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		
		NBTTagList nbttaglist = nbttagcompound.getTagList("items");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {    		
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		items [index] = new ItemStack(nbttagcompound2);
    	}    	
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	
		NBTTagList nbttaglist = new NBTTagList();
    	
    	for (int j = 0; j < items.length; ++j) {    		    		
    		if (items [j] != null && items [j].stackSize > 0) {
        		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
        		nbttaglist.setTag(nbttagcompound2);
    			nbttagcompound2.setInteger("index", j);
    			items [j].writeToNBT(nbttagcompound2);	
    		}     		
    	}
    	
    	nbttagcompound.setTag("items", nbttaglist);    	
    }
    
    public void destroy () {
    	super.destroy();
    	
		Utils.dropItems(worldObj, this, xCoord, yCoord, zCoord);
    }

	@Override
	public boolean addItemFromPipe(ItemStack stack, boolean doAdd, Orientations from) {
		return false;
	}

	@Override
	public ItemStack extractItemToPipe(boolean doRemove, Orientations from) {
		return null;
	}
}
