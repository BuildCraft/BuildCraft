package buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.SimpleInventory;

public class TileFilteredBuffer extends TileBuildCraft implements IInventory, IOverrideDefaultTriggers {
	
	private final SimpleInventory inventoryFilters = new SimpleInventory(9, "FilteredBufferFilters", 1);
	private final SimpleInventory inventoryStorage = new SimpleInventory(9, "FilteredBufferStorage", 64);

	@Override
	public void updateEntity() {
		super.updateEntity();
	}
	
	public IInventory getFilters() {
		return inventoryFilters;
	}

	/** IInventory Implementation **/

	@Override
	public int getSizeInventory() {
		return inventoryStorage.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return inventoryStorage.getStackInSlot(slotId);
	}

	@Override
	public ItemStack decrStackSize(int slotId, int count) {
		return inventoryStorage.decrStackSize(slotId, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return inventoryStorage.getStackInSlotOnClosing(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		inventoryStorage.setInventorySlotContents(slotId, itemStack);
	}

	@Override
	public String getInvName() {
		return inventoryStorage.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inventoryStorage.getInventoryStackLimit();
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

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {

		ItemStack filterItemStack = inventoryFilters.getStackInSlot(i);
		
		if ( filterItemStack == null || filterItemStack.itemID != itemstack.itemID)
			return false;
		
		if (Item.itemsList[itemstack.itemID].isDamageable())
			return true;
		
		if (filterItemStack.getItemDamage() == itemstack.getItemDamage())
			return true;
		
		return false;
	}
	
	@Override
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>();

		result.add(BuildCraftTransport.triggerInventoryBelow25);
		result.add(BuildCraftTransport.triggerInventoryBelow50);
		result.add(BuildCraftTransport.triggerInventoryBelow75);

		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		NBTTagCompound inventoryStorageTag = (NBTTagCompound) nbtTagCompound.getTag("inventoryStorage");
		inventoryStorage.readFromNBT(inventoryStorageTag);
		NBTTagCompound inventoryFiltersTag = (NBTTagCompound) nbtTagCompound.getTag("inventoryFilters");
		inventoryFilters.readFromNBT(inventoryFiltersTag);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		NBTTagCompound inventoryStorageTag = new NBTTagCompound();
		inventoryStorage.writeToNBT(inventoryStorageTag);
		nbtTagCompound.setTag("inventoryStorage", inventoryStorageTag);

		NBTTagCompound inventoryFiltersTag = new NBTTagCompound();
		inventoryFilters.writeToNBT(inventoryFiltersTag);
		nbtTagCompound.setTag("inventoryFilters", inventoryFiltersTag);
	}
}
