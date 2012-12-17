package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.InventoryUtil;
import buildcraft.core.utils.SidedInventoryAdapter;
import buildcraft.core.utils.SimpleInventory;

public class TileHopper extends TileBuildCraft implements IInventory {

	private final SimpleInventory _inventory = new SimpleInventory(4, "Hopper", 64);
	private final InventoryUtil _internalInventory = new InventoryUtil(_inventory);

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
		if (CoreProxy.proxy.isRenderWorld(worldObj) || worldObj.getWorldTime() % 5 != 0)
			return;
		int internalSlot = _internalInventory.getIdForFirstSlot();
		if (internalSlot < 0)
			return;

		TileEntity tile = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);

		if (tile instanceof ISpecialInventory) {
			ISpecialInventory special = (ISpecialInventory) tile;
			ItemStack clonedStack = _inventory.getStackInSlot(internalSlot).copy().splitStack(1);
			if (special.addItem(clonedStack, true, ForgeDirection.UP) > 0) {
				_inventory.decrStackSize(internalSlot, 1);
			}
			return;
		}

		if (!(tile instanceof IInventory))
			return;
		IInventory inventory = (IInventory) tile;
		if (tile instanceof ISidedInventory) {
			inventory = new SidedInventoryAdapter((ISidedInventory) tile, ForgeDirection.UP);
		}

		InventoryUtil externalInventory = new InventoryUtil(inventory);
		if (externalInventory.hasRoomForItem(_inventory.getStackInSlot(internalSlot))) {
			ItemStack stackToMove = _inventory.decrStackSize(internalSlot, 1);
			externalInventory.addToInventory(stackToMove);
		}
	}

	/** IInventory Implementation **/

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
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}
}
