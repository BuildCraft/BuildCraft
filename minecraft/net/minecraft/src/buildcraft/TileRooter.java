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
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class TileRooter extends TileEntity implements IInventory, IPipeEntry, ITickListener {

	class ItemInTransit {
		EntityPassiveItem item;
		Orientations orientation = Orientations.Unknown;
		long exitDate;
	}
	
	LinkedList<ItemInTransit> itemsInTransit = new LinkedList<ItemInTransit>();
	LinkedList<ItemInTransit> itemsToLoad = new LinkedList<ItemInTransit>();
	
	ItemStack [] items = new ItemStack [48];
	
	public TileRooter () {
		items = new ItemStack [getSizeInventory()];
	}
	
	@Override
	public int getSizeInventory() { 
		return 48;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// TODO Auto-generated method stub
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
		return null;
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
	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
		if (itemsInTransit.size() == 0) {
			mod_BuildCraft.getInstance().registerTicksListener(this, 1);			
		}
		
		ItemInTransit newItem = new ItemInTransit();
		
		World w = ModLoader.getMinecraftInstance().theWorld;		
		
		newItem.exitDate = w.getWorldTime() + 50;
		newItem.item = item;			
		newItem.item.setPosition(xCoord + 0.5, yCoord + 0.4, zCoord + 0.5);
		newItem.orientation = orientation;
		
		itemsInTransit.add(newItem);		
	}

	@Override
	public Position getPosition() {
		return new Position (xCoord, yCoord, zCoord);
	}

	@Override
	public void tick(Minecraft minecraft) {
		World w = ModLoader.getMinecraftInstance().theWorld;
		
		for (ItemInTransit item : itemsToLoad) {
			w.entityJoinedWorld(item.item);
			itemsInTransit.add(item);
		}
		
		itemsToLoad.clear();
		
		LinkedList<ItemInTransit> itemsToRemove = new LinkedList<ItemInTransit>();						
		
		for (ItemInTransit data : itemsInTransit) {
			if (data.exitDate <= w.getWorldTime()) {
				Orientations lastLeakOrientation = Orientations.Unknown;
				Orientations exit = Orientations.Unknown;
				
				for (int dir = 0; dir <= 5; ++dir) {
					if (dir == data.orientation.reverse().ordinal()) {
						//  Do noot root to origin
						continue;
					}
					
					boolean foundFilter = false;
					
					for (int slot = 0; slot < 8; ++slot) {
						ItemStack stack = getStackInSlot(dir * 8 + slot);
						
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
						
						TileEntity tile = w.getBlockTileEntity((int) pos.i, (int) pos.j, (int) pos.k);
						
						if (tile instanceof IPipeEntry) {
							exit = pos.orientation;
						} else if (w.getBlockId((int) pos.i, (int) pos.j, (int) pos.k) == 0) {
							lastLeakOrientation = pos.orientation;
						}
					}
					
				}
				
				if (exit == Orientations.Unknown) {
					exit = lastLeakOrientation;
				}
								
				itemsToRemove.add(data);
								
				Position itemPos = new Position(xCoord + 0.5, yCoord + 0.4,
						zCoord + 0.5, exit);
				itemPos.moveForwards(0.61);
				data.item.setPosition(itemPos.i, itemPos.j, itemPos.k);				
				
				Position pos = new Position (xCoord, yCoord, zCoord, exit);
				pos.moveForwards(1);
				TileEntity tile = w.getBlockTileEntity((int) pos.i, (int) pos.j, (int) pos.k);				

				if (tile instanceof IPipeEntry) {														
					((IPipeEntry) tile).entityEntering(data.item, exit);					
				} else {
					data.item.toEntityItem(w, exit, 0.1F);
				}
			}
		}
		
		itemsInTransit.removeAll(itemsToRemove);
		
		if (itemsInTransit.size() == 0) {			
			mod_BuildCraft.getInstance().unregisterTicksListener(this);
		}
		
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
    	
    	nbttaglist = nbttagcompound.getTagList("itemsInTransit");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		ItemInTransit data = new ItemInTransit();
    		data.item = new EntityPassiveItem(w);
    		data.item.readEntityFromNBT(nbttagcompound2);
    		data.exitDate = nbttagcompound2.getLong("exitDate");
    		data.orientation = Orientations.values() [nbttagcompound2.getInteger("orientation")];
    		
    		itemsToLoad.add(data);
    	}

    	if (itemsToLoad.size() > 0) {
    		mod_BuildCraft.getInstance().registerTicksListener(this, 1);
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
    	
    	nbttaglist = new NBTTagList();
    	
    	for (ItemInTransit data : itemsInTransit) {    		
    		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
    		nbttaglist.setTag(nbttagcompound2);
    		data.item.writeToNBT(nbttagcompound2);
    		nbttagcompound2.setLong("exitDate", data.exitDate);
    		nbttagcompound2.setInteger("orientation", data.orientation.ordinal());
    	}

    	nbttagcompound.setTag("itemsInTransit", nbttaglist);
    }
    


}
