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
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.core.network.TilePacketWrapper;

public class PipeLogicDiamond extends PipeLogic {

	ItemStack [] items = new ItemStack [54];

	public class PacketStack {
		@TileNetworkData (intKind = TileNetworkData.UNSIGNED_BYTE)
		public int num;

		@TileNetworkData (staticSize = 9)
		public short [] ids = new short [9];

		@TileNetworkData (staticSize = 9, intKind = TileNetworkData.UNSIGNED_BYTE)
		public int [] dmg = new int [9];
	}

	private static TilePacketWrapper networkPacket;

	private final SafeTimeTracker tracker = new SafeTimeTracker();

	public PipeLogicDiamond () {
		if (networkPacket == null)
			networkPacket = new TilePacketWrapper(new Class[] {
					PacketStack.class });
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null
				&& entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length)
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe)
				return false;

		if(!APIProxy.isClient(container.worldObj))
			entityplayer.openGui(mod_BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.worldObj, container.xCoord, container.yCoord, container.zCoord);

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

		if (items [i].stackSize == 0)
			items [i] = null;

		if (APIProxy.isServerSide())
			for (int p = 0; p < 6; ++p)
				CoreProxy.sendToPlayers(
						getContentsPacket(p), worldObj, xCoord, yCoord, zCoord,
						DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftTransport.instance);

		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if (items [i] == null && itemstack == null)
			return;
		else if (items [i] != null && itemstack != null && items[i].isStackEqual(itemstack))
			return;

		if (itemstack != null)
			items [i] = itemstack.copy();
		else
			items [i] = null;

		if (APIProxy.isServerSide())
			for (int p = 0; p < 6; ++p)
				CoreProxy.sendToPlayers(
						getContentsPacket(p), worldObj, xCoord, yCoord, zCoord,
						DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftTransport.instance);
	}

	@Override
	public void updateEntity () {
		if (tracker.markTimeIfDelay(worldObj, 20 * BuildCraftCore.updateFactor))
			if (APIProxy.isServerSide())
				for (int p = 0; p < 6; ++p)
					CoreProxy.sendToPlayers(
							getContentsPacket(p), worldObj, xCoord, yCoord, zCoord,
							DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftTransport.instance);
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
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("items");

    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		items [index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);

		NBTTagList nbttaglist = new NBTTagList();

    	for (int j = 0; j < items.length; ++j)
			if (items [j] != null && items [j].stackSize > 0) {
        		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
        		nbttaglist.appendTag(nbttagcompound2);
    			nbttagcompound2.setInteger("index", j);
    			items [j].writeToNBT(nbttagcompound2);
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

	/** SERVER SIDE **/
	public Packet getContentsPacket(int num) {
		PacketStack stacks = new PacketStack();
		stacks.num = num;

		for (int j = 0; j < 9; ++j)
			if (items [j + num * 9] == null) {
				stacks.ids [j] = -1;
				stacks.dmg [j] = -1;
			} else {
				stacks.ids [j] = (short) items [j + num * 9].itemID;
				stacks.dmg [j] = items [j + num * 9].getItemDamage();
			}

		return new PacketUpdate(PacketIds.DIAMOND_PIPE_CONTENTS, xCoord, yCoord, zCoord, networkPacket.toPayload(stacks)).getPacket();

    }

	/** CLIENT SIDE **/
	public void handleContentsPacket (PacketUpdate packet) {
		PacketStack stacks = new PacketStack();

		networkPacket.fromPayload(stacks, packet.payload);

		int num = stacks.num;

		for (int j = 0; j < 9; ++j)
			if (stacks.ids [j] == -1)
				items [num * 9 + j] = null;
			else
				items[num * 9 + j] = new ItemStack(stacks.ids [j], 1,
						stacks.dmg [j]);
	}

	@Override
	public boolean doDrop() {
		return false;
	}

}
