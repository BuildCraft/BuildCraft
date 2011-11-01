/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.PacketIds;

public class PipeLogicDiamond extends PipeLogic {
	
	ItemStack [] items = new ItemStack [54];
	
	private SafeTimeTracker tracker = new SafeTimeTracker();
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {		
		if (entityplayer.getCurrentEquippedItem() != null
				&& entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length) {
			
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe) {
				return false;
			}
		}
		
		TransportProxy.displayGUIFilter(entityplayer, this.container);
		
		return true;		
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
		
		if (APIProxy.isServerSide()) {
			CoreProxy.sendToPlayers(
					(Packet230ModLoader) getContentsPacket(), xCoord,
					yCoord, zCoord, 50, mod_BuildCraftTransport.instance);
		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
		if (APIProxy.isServerSide()) {
			CoreProxy.sendToPlayers(
					(Packet230ModLoader) getContentsPacket(), xCoord,
					yCoord, zCoord, 50, mod_BuildCraftTransport.instance);
		}
	}
	
	@Override
	public void updateEntity () {
		if (tracker.markTimeIfDelay(worldObj, 200)) {
			if (APIProxy.isServerSide()) {
				CoreProxy.sendToPlayers(
						(Packet230ModLoader) getContentsPacket(), xCoord,
						yCoord, zCoord, 50, mod_BuildCraftTransport.instance);
			}
		}
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
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		
		NBTTagList nbttaglist = nbttagcompound.getTagList("items");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {    		
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		items [index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
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

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return false;
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		return null;
	}
	
	public Packet getContentsPacket() {
		Packet230ModLoader packet = new Packet230ModLoader();

		packet.modId = mod_BuildCraftTransport.instance.getId();
		packet.packetType = PacketIds.DiamondPipeContents.ordinal();
		packet.isChunkDataPacket = true;

		packet.dataInt = new int [3 + items.length * 2];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		
		for (int j = 0; j < items.length; ++j) {
			if (items [j] == null) {
				packet.dataInt [3 + j * 2 + 0] = -1;
				packet.dataInt [3 + j * 2 + 1] = -1;
			} else {
				packet.dataInt [3 + j * 2 + 0] = items [j].itemID;
				packet.dataInt [3 + j * 2 + 1] = items [j].getItemDamage();
			}
			 
		}
		
		return packet;
    }
	
	public void handleContentsPacket (Packet230ModLoader packet) {
		if (packet.packetType != PacketIds.DiamondPipeContents.ordinal()) {
			return;
		}
		
		for (int j = 0; j < items.length; ++j) {
			if (packet.dataInt [3 + j * 2 + 0] == -1) {
				items [j] = null;
			} else {
				items[j] = new ItemStack(packet.dataInt[3 + j * 2 + 0], 1,
						packet.dataInt[3 + j * 2 + 1]);
			}			 
		}
	}

}
