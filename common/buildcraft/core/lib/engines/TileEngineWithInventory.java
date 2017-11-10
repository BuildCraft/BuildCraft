/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.engines;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.utils.Utils;

public abstract class TileEngineWithInventory extends TileEngineBase implements IInventory, ISidedInventory {

	private final SimpleInventory inv;
	private final int[] defaultSlotArray;

	public TileEngineWithInventory(int invSize) {
		inv = new SimpleInventory(invSize, "Engine", 64);
		defaultSlotArray = Utils.createSlotArray(0, invSize);
	}

	/* IINVENTORY IMPLEMENTATION */
	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return inv.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		inv.setInventorySlotContents(slot, itemstack);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public String getInventoryName() {
		return "Engine";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		inv.readFromNBT(data);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		inv.writeToNBT(data);
	}

	// ISidedInventory

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if (side == orientation.ordinal()) {
			return new int[0];
		} else {
			return defaultSlotArray;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return side != orientation.ordinal();
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return side != orientation.ordinal();
	}
}