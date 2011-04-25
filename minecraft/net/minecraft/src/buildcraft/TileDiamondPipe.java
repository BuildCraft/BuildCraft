package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;
import net.minecraft.src.buildcraft.TileRooter.ItemInTransit;

public class TileDiamondPipe extends TilePipe implements IInventory {

//    protected Orientations resolveDestination (EntityData data) {
//    	LinkedList<Orientations> listOfPossibleMovements = getPossibleMovements(new Position(
//				xCoord, yCoord, zCoord, data.orientation), data.item);
//		
//		if (listOfPossibleMovements.size() == 0) {					
//			return Orientations.Unknown;													
//		} else {					
//			int i = world.rand.nextInt(listOfPossibleMovements.size());
//			
//			return listOfPossibleMovements.get(i);															
//		}				
//    }
    
	LinkedList<ItemInTransit> itemsToLoad = new LinkedList<ItemInTransit>();
	
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
		items [i].stackSize -= j;		
		
		return new ItemStack(items [i].getItem(), j);
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
//
//	@Override
//	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
//		if (itemsInTransit.size() == 0) {
//			mod_BuildCraft.getInstance().registerTicksListener(this, 1);			
//		}
//		
//		ItemInTransit newItem = new ItemInTransit();
//		
//		World w = ModLoader.getMinecraftInstance().theWorld;		
//		
//		newItem.exitDate = w.getWorldTime() + 50;
//		newItem.item = item;			
//		newItem.item.setPosition(xCoord + 0.5,
//				yCoord + Utils.getPipeFloorOf(item.item), zCoord + 0.5);
//		newItem.orientation = orientation;
//		
//		itemsInTransit.add(newItem);		
//	}

	@Override
  protected Orientations resolveDestination (EntityData data) {
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
		
		World w = ModLoader.getMinecraftInstance().theWorld;		
		
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
