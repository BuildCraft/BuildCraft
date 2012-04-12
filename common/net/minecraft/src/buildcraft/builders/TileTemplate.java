/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileTemplate extends TileBuildCraft implements IInventory {
	
	public @TileNetworkData Box box = new Box ();
	
	private ItemStack items [] = new ItemStack [2];
	
	private boolean isComputing = false;
	public int computingTime = 0;
	
	//  Use that field to avoid creating several times the same template if 
	//  they're the same!
	private int lastTemplateId = 0;
	
	public void updateEntity () {
		super.updateEntity();
		
		if (isComputing) {
			if (computingTime < 200) {
				computingTime++;
			} else {
				createBluePrint();
			}
		}		
	}	
	
	@Override
    public void initialize () {
		super.initialize();
		
		if (!box.isInitialized()) {
			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord,
					yCoord, zCoord);

			if (a != null) {
				box.initialize(a);
				a.removeFromWorld();

			}
		}
		
		if (!APIProxy.isClient(worldObj) && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
				
		sendNetworkUpdate();
    }
    
    public void createBluePrint () {
    	if (!box.isInitialized() || items [1] != null) {
    		return;
    	}
    	
    	int mask1 = 1;
    	int mask0 = 0;
    	
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord)) {
			mask1 = 0;
			mask0 = 1;
		}
    	
    	BluePrint result = new BluePrint(box.sizeX(), box.sizeY(), box.sizeZ());
    	
    	for (int x = box.xMin; x <= box.xMax; ++x) {
    		for (int y = box.yMin; y <= box.yMax; ++y) {
    			for (int z = box.zMin; z <= box.zMax; ++z) {
    				if (worldObj.getBlockId(x, y, z) != 0) {    					
						result.setBlockId(x - box.xMin, y - box.yMin, z
								- box.zMin, mask1);
    				} else {
    					result.setBlockId(x - box.xMin, y - box.yMin, z
								- box.zMin, mask0);
    				}
    			}
    		}
    	}
    	
    	result.anchorX = xCoord - box.xMin;
    	result.anchorY = yCoord - box.yMin;
    	result.anchorZ = zCoord - box.zMin;
    	 
		Orientations o = Orientations.values()[worldObj.getBlockMetadata(xCoord,
				yCoord, zCoord)].reverse();
		
		if (o == Orientations.XPos) {
			// Do nothing
		} else if (o == Orientations.ZPos) {
			result.rotateLeft();
			result.rotateLeft();
			result.rotateLeft();
		} else if (o == Orientations.XNeg) {
			result.rotateLeft();
			result.rotateLeft();
		} else if (o == Orientations.ZNeg) {
			result.rotateLeft();
		}
    
		ItemStack stack = new ItemStack(BuildCraftBuilders.templateItem);
		
		if (result.equals(BuildCraftBuilders.bluePrints[lastTemplateId])) {
			result = BuildCraftBuilders.bluePrints[lastTemplateId];
			stack.setItemDamage(lastTemplateId);
		} else {
			int bptId = BuildCraftBuilders.storeBluePrint(result);
			stack.setItemDamage(bptId);
			CoreProxy.addName(stack, "Template #" + bptId);
			lastTemplateId = bptId;
		}
    	
    	setInventorySlotContents(0, null);
    	setInventorySlotContents(1, stack);
    }

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items [i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items [i] == null) {
			result = null;
		} else if (items [i].stackSize > j) {
			result = items [i].splitStack(j);
		} else {
			ItemStack tmp = items [i];
			items [i] = null;
			result = tmp;
		}
		
		initializeComputing();
		
		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
		initializeComputing();
		
	}

	@Override
	public String getInvName() {
		return "Template";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		lastTemplateId = nbttagcompound.getInteger("lastTemplateId");
		computingTime = nbttagcompound.getInteger("computingTime");
		isComputing = nbttagcompound.getBoolean("isComputing");
		
		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}
		
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        items = new ItemStack[getSizeInventory()];
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;
            if(j >= 0 && j < items.length)
            {
                items[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("lastTemplateId", lastTemplateId);
		nbttagcompound.setInteger("computingTime", computingTime);
		nbttagcompound.setBoolean("isComputing", isComputing);
		
		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}
		
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < items.length; i++)
        {
            if(items[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                items[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
	}
	
	@Override
	public void invalidate () {
		destroy ();
	}
	
	@Override
    public void destroy () {
    	if (box.isInitialized()) {
    		box.deleteLasers();    		
    	}    	
    }
    
    private void initializeComputing () {    	
    	if (!box.isInitialized()) {
    		return;
    	} else if (!isComputing) {
			if (items[0] != null && items[0].getItem() instanceof ItemTemplate
					&& items[1] == null) {
    			isComputing = true;
    			computingTime = 0;
    		} else {
    			isComputing = false;
    			computingTime = 0;
    		}
    	} else {
    		if (items [0] == null || !(items [0].getItem() instanceof ItemTemplate)) {
    			isComputing = false;
    			computingTime = 0;
    		}
    	}
    }

    public int getComputingProgressScaled(int i) {
        return (computingTime * i) / 200;
    }
    
    @Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleDescriptionPacket(packet);		
				
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);			
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleUpdatePacket(packet);
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int var1){
		if (this.items[var1] == null) return null;
		ItemStack stack = this.items[var1];
		this.items[var1] = null;
		return stack;
	}


}
