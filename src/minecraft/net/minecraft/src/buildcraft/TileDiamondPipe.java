package net.minecraft.src.buildcraft;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

public class TileDiamondPipe extends TilePipe implements IInventory {
	
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
	protected Orientations resolveDestination(EntityData data) {
		Orientations lastLeakOrientation = Orientations.Unknown;
		Orientations exit = Orientations.Unknown;
				
		for (int dir = 0; dir <= 5; ++dir) {
			if (dir == data.orientation.reverse().ordinal()) {
				//  Do noot root to origin
				continue;
			}

			boolean foundFilter = false;

			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = getStackInSlot(dir * 9 + slot);

				if (stack != null) {
					foundFilter = true;
				}

				if (stack != null && stack.itemID == data.item.item.itemID) {					
					exit = Orientations.values() [dir];
				}
			}
					
			if (exit == Orientations.Unknown && !foundFilter) {								
				Position pos = new Position (xCoord, yCoord, zCoord, Orientations.values() [dir]);
				pos.moveForwards(1);

				TileEntity tile = world.getBlockTileEntity((int) pos.i, (int) pos.j, (int) pos.k);

				if (tile instanceof IPipeEntry) {
					exit = pos.orientation;
				} else if (world.getBlockId((int) pos.i, (int) pos.j, (int) pos.k) == 0) {
					lastLeakOrientation = pos.orientation;
				}
			}

		}
				
		if (exit == Orientations.Unknown) {
			exit = lastLeakOrientation;
		}
		
		return exit;													
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
}
