/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.IDropControlInventory;

public class PipeLogic implements IDropControlInventory {

	public int xCoord;
	public int yCoord;
	public int zCoord;
	public World worldObj;
	public TileGenericPipe container;

	public void setPosition (int xCoord, int yCoord, int zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	public void setWorld (World worldObj) {
		this.worldObj = worldObj;
	}

	public boolean blockActivated(EntityPlayer entityplayer) {

		return false;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {

	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {

	}

	public void setTile(TileGenericPipe tile) {
		this.container = tile;
	}

	public boolean isPipeConnected(TileEntity tile) {
		return true;
	}

	public void onNeighborBlockChange(int blockId) {

	}

	public void onBlockPlaced() {

	}

	public void initialize() {

	}

	public boolean inputOpen(Orientations from) {
		return true;
	}

	public boolean outputOpen(Orientations to) {
		return true;
	}

	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return false;
	}

	public ItemStack extractItem(boolean doRemove, Orientations from) {
		return null;
	}

	public int getSizeInventory() {
		return 0;
	}

	public ItemStack getStackInSlot(int i) {
		return null;
	}

	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	public void setInventorySlotContents(int i, ItemStack itemstack) {

	}

	public String getInvName() {
		return null;
	}

	public int getInventoryStackLimit() {
		return 0;
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void updateEntity() {

	}

	@Override
	public boolean doDrop() {
		return true;
	}
}
