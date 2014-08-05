/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.commander;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.WorldBlockIndex;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;

public class TileRequester extends TileBuildCraft implements IInventory {

	public static final int NB_ITEMS = 20;

	private SimpleInventory inv = new SimpleInventory(NB_ITEMS, "items", 64);

	private ArrayList<StackRequest> requirements = new ArrayList<StackRequest>();

	public TileRequester() {
		for (int i = 0; i < NB_ITEMS; ++i) {
			requirements.add(null);
		}
	}

	public boolean addRequest(ItemStack stack, Entity from) {
		for (int i = 0; i < NB_ITEMS; ++i) {
			if (requirements.get(i) == null) {
				StackRequest r = new StackRequest();

				r.fulfilled = false;
				r.holder = new WorldBlockIndex(worldObj, xCoord, yCoord, zCoord);
				r.indexInHolder = i;
				r.loadDate = worldObj.getTotalWorldTime();
				r.requestDate = worldObj.getTotalWorldTime();
				r.requester = from;
				r.stack = stack;

				requirements.set(i, r);

				return true;
			}
		}

		return false;
	}

	@Override
	public int getSizeInventory() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inv.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return inv.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return inv.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		inv.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public String getInventoryName() {
		return inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return inv.isUseableByPlayer(entityPlayer);
	}

	@Override
	public void openInventory() {
		inv.openInventory();
	}

	@Override
	public void closeInventory() {
		inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		StackRequest req = requirements.get(i);

		if (req == null) {
			return false;
		} else if (!StackHelper.isMatchingItem(req.stack, itemStack)) {
			return false;
		} else {
			return inv.isItemValidForSlot(i, itemStack);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagCompound invNBT = new NBTTagCompound();
		inv.writeToNBT(invNBT);
		nbt.setTag("inv", invNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		System.out.println("READ");

		inv.readFromNBT(nbt.getCompoundTag("inv"));

	}
}
