/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.Transactor;

public class TileHopper extends TileBuildCraft implements IInventory {

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
	public void updateEntity() {
		super.updateEntity();
		if (worldObj.isRemote || isEmpty ||
				worldObj.getTotalWorldTime() % 2 != 0) {
			return;
		}

		TileEntity outputTile = getTile(ForgeDirection.DOWN);

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
			if (transactor.add(clonedStack, ForgeDirection.UP, true).stackSize > 0) {
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
	public String getInventoryName() {
		return inventory.getInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && entityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
