package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.proxy.CoreProxy;

public class TileHopper extends TileBuildCraft implements IInventory {

	private final SimpleInventory _inventory = new SimpleInventory(4, "Hopper", 64);

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);
		NBTTagCompound p = (NBTTagCompound) nbtTagCompound.getTag("inventory");
		_inventory.readFromNBT(p);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		NBTTagCompound inventoryTag = new NBTTagCompound();
		_inventory.writeToNBT(inventoryTag);
		nbtTagCompound.setTag("inventory", inventoryTag);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (CoreProxy.proxy.isRenderWorld(worldObj) || worldObj.getTotalWorldTime() % 2 != 0)
			return;

		TileEntity tile = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);

		if (tile == null)
			return;

		ITransactor transactor = Transactor.getTransactorFor(tile);

		if (transactor == null)
			return;

		for (int internalSlot = 0; internalSlot < _inventory.getSizeInventory(); internalSlot++) {
			ItemStack stackInSlot = _inventory.getStackInSlot(internalSlot);
			if (stackInSlot == null)
				continue;

			ItemStack clonedStack = stackInSlot.copy().splitStack(1);
			if (transactor.add(clonedStack, ForgeDirection.UP, true).stackSize > 0) {
				_inventory.decrStackSize(internalSlot, 1);
				return;
			}
		}
	}

	/**
	 * IInventory Implementation *
	 */
	@Override
	public int getSizeInventory() {
		return _inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return _inventory.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return _inventory.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return _inventory.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		_inventory.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public String getInvName() {
		return _inventory.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return _inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && entityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
}
