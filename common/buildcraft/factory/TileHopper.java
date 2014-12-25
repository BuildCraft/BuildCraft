/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.Transactor;

public class TileHopper extends TileBuildCraft implements IInventory, IHopper {

	private final SimpleInventory inventory = new SimpleInventory(4, "Hopper", 64);
	private boolean isEmpty;

    @Override
    public void initialize() {
        inventory.addListener(this);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		NBTTagCompound p = nbtTagCompound;

		if (nbtTagCompound.hasKey("inventory")) {
			// to support pre 6.0 loading
			p = nbtTagCompound.getCompoundTag("inventory");
		}

		inventory.readFromNBT(p);
		inventory.markDirty();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		inventory.writeToNBT(nbtTagCompound);
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote || isEmpty ||
				worldObj.getTotalWorldTime() % 2 != 0) {
			return;
		}

		TileEntity outputTile = getTile(EnumFacing.DOWN);

		ITransactor transactor = Transactor.getTransactorFor(outputTile);
		
		if (transactor == null) {
			return;
		}
		
		for (int internalSlot = 0; internalSlot < inventory.getSizeInventory(); internalSlot++) {
			ItemStack stackInSlot = inventory.getStackInSlot(internalSlot);
			if (stackInSlot == null || stackInSlot.stackSize == 0) {
				continue;
			}
			
			ItemStack clonedStack = stackInSlot.copy().splitStack(1);
			if (transactor.add(clonedStack, EnumFacing.UP, true).stackSize > 0) {
				inventory.decrStackSize(internalSlot, 1);
				return;
			}
		}
	}

    @Override
	public void markDirty() {
		isEmpty = true;
		
		for (int internalSlot = 0; internalSlot < inventory.getSizeInventory(); internalSlot++) {
			ItemStack stackInSlot = inventory.getStackInSlot(internalSlot);
			if (stackInSlot != null && stackInSlot.stackSize > 0) {
				isEmpty = false;
				return;
			}
		}
	}
	
	/**
	 * IInventory Implementation *
	 */
	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inventory.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		ItemStack output = inventory.decrStackSize(slotId, count);
		return output;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		ItemStack output = inventory.getStackInSlotOnClosing(slotId);
		return output;
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		inventory.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public void openInventory(EntityPlayer playerIn) {

	}

	@Override
	public void closeInventory(EntityPlayer playerIn) {

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public String getName() {
		return inventory.getName();
	}

	@Override
	public double getXPos() {
		return pos.getX();
	}

	@Override
	public double getYPos() {
		return pos.getY();
	}

	@Override
	public double getZPos() {
		return pos.getZ();
	}
}
