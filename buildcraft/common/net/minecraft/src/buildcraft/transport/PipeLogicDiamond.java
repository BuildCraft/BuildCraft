package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.buildcraft.api.Orientations;

public class PipeLogicDiamond extends PipeLogic {
	
	ItemStack [] items = new ItemStack [54];
	
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
		
//		if (APIProxy.isServerSide()) {
//			CoreProxy.sendToPlayers(
//					(Packet230ModLoader) getDescriptionPacket(), xCoord,
//					yCoord, zCoord, 50, mod_BuildCraftTransport.instance);
//		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
//		if (APIProxy.isServerSide()) {
//			CoreProxy.sendToPlayers(
//					(Packet230ModLoader) getDescriptionPacket(), xCoord,
//					yCoord, zCoord, 50, mod_BuildCraftTransport.instance);
//		}

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

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return false;
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		return null;
	}
	
//	@Override
//	public void initialize () {
//		super.initialize();
//		
//		BlockIndex index = new BlockIndex(xCoord, yCoord, zCoord);
//		
//		if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {
//			Packet230ModLoader packet = BuildCraftCore.bufferedDescriptions.get(index);
//			BuildCraftCore.bufferedDescriptions.remove(index);
//			
//			handlePacket(packet);
//		}
//	}
	
//	public Packet getDescriptionPacket() {
//		Packet230ModLoader packet = new Packet230ModLoader();
//
//		packet.modId = mod_BuildCraftTransport.instance.getId();
//		packet.packetType = PacketIds.DiamondPipeContents.ordinal();
//		packet.isChunkDataPacket = true;
//
//		packet.dataInt = new int [3 + items.length * 2];
//		
//		packet.dataInt [0] = xCoord;
//		packet.dataInt [1] = yCoord;
//		packet.dataInt [2] = zCoord;
//		
//		for (int j = 0; j < items.length; ++j) {
//			if (items [j] == null) {
//				packet.dataInt [3 + j * 2 + 0] = -1;
//				packet.dataInt [3 + j * 2 + 1] = -1;
//			} else {
//				packet.dataInt [3 + j * 2 + 0] = items [j].itemID;
//				packet.dataInt [3 + j * 2 + 1] = items [j].getItemDamage();
//			}
//			 
//		}
//		
//		return packet;
//    }
//	
//	public void handlePacket (Packet230ModLoader packet) {
//		if (packet.packetType != PacketIds.DiamondPipeContents.ordinal()) {
//			return;
//		}
//		
//		for (int j = 0; j < items.length; ++j) {
//			if (packet.dataInt [3 + j * 2 + 0] == -1) {
//				items [j] = null;
//			} else {
//				items[j] = new ItemStack(packet.dataInt[3 + j * 2 + 0], 1,
//						packet.dataInt[3 + j * 2 + 1]);
//			}			 
//		}
//	}

}
