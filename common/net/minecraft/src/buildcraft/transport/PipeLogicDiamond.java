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
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.core.Orientations;
import net.minecraft.src.buildcraft.api.core.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.inventory.ISpecialInventory;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketNBT;
import net.minecraft.src.buildcraft.core.utils.SimpleInventory;

public class PipeLogicDiamond extends PipeLogic implements ISpecialInventory {

	private SimpleInventory filters = new SimpleInventory(54, "items", 1);
	private final SafeTimeTracker tracker = new SafeTimeTracker();

	/* PIPE LOGIC */
	@Override
	public boolean doDrop() {
		return false;
	}
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null
				&& entityplayer.getCurrentEquippedItem().itemID < Block.blocksList.length)
			if (Block.blocksList[entityplayer.getCurrentEquippedItem().itemID] instanceof BlockGenericPipe)
				return false;

		if (!APIProxy.isClient(container.worldObj))
			entityplayer.openGui(mod_BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.worldObj, container.xCoord,
					container.yCoord, container.zCoord);

		return true;
	}

	/* UPDATING */
	@Override
	public void updateEntity() {
		if (tracker.markTimeIfDelay(worldObj, 20 * BuildCraftCore.updateFactor))
			if (APIProxy.isServerSide())
				sendFilterSet();
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		filters.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		filters.writeToNBT(nbttagcompound);
	}

	/* ISPECIALINVENTORY */
	@Override
	public int addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return 0;
	}
	@Override
	public ItemStack[] extractItem(boolean doRemove, Orientations from, int maxItemCount) {
		return new ItemStack[0];
	}

	/* IINVENTORY IMPLEMENTATION */
	@Override public int getSizeInventory() { return filters.getSizeInventory(); }
	@Override public ItemStack getStackInSlot(int i) { return filters.getStackInSlot(i); }
	@Override public String getInvName() { return "Filters"; }
	@Override public int getInventoryStackLimit() { return filters.getInventoryStackLimit(); }
	@Override public ItemStack getStackInSlotOnClosing(int i) { return filters.getStackInSlotOnClosing(i); }
	@Override public void onInventoryChanged() { filters.onInventoryChanged(); }
	@Override public boolean isUseableByPlayer(EntityPlayer var1) { return true; }
	@Override public void openChest() {}
	@Override public void closeChest() {}
	
	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack stack = filters.decrStackSize(i, j);

		if (APIProxy.isServerSide())
			sendFilterSet();

		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {

		filters.setInventorySlotContents(i, itemstack);		
		if (APIProxy.isServerSide())
			sendFilterSet();
		
	}

	/* SERVER SIDE */
	public void sendFilterSet() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		PacketNBT packet = new PacketNBT(PacketIds.DIAMOND_PIPE_CONTENTS, nbttagcompound, xCoord, yCoord, zCoord);
		CoreProxy.sendToPlayers(packet.getPacket(), worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftTransport.instance);
	}

	/* CLIENT SIDE */
	public void handleFilterSet(PacketNBT packet) {
		this.readFromNBT(packet.getTagCompound());
	}

}
