package buildcraft.core.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public abstract class InventoryWrapper implements ISidedInventory {

	IInventory inventory;

	public InventoryWrapper(IInventory inventory) {
		this.inventory = inventory;
	}

	/* DIRECT MAPPING */
	@Override public int getSizeInventory() { return inventory.getSizeInventory(); }
	@Override public ItemStack getStackInSlot(int slotIndex) { return inventory.getStackInSlot(slotIndex); }
	@Override public ItemStack decrStackSize(int slotIndex, int amount) { return inventory.decrStackSize(slotIndex, amount); }
	@Override public ItemStack getStackInSlotOnClosing(int slotIndex) { return inventory.getStackInSlotOnClosing(slotIndex); }
	@Override public void setInventorySlotContents(int slotIndex, ItemStack itemstack) { inventory.setInventorySlotContents(slotIndex, itemstack); }
	@Override public String getInventoryName() { return inventory.getInventoryName(); }
	@Override public boolean isInvNameLocalized() { return inventory.isInvNameLocalized(); }
	@Override public int getInventoryStackLimit() { return inventory.getInventoryStackLimit(); }
	@Override public void onInventoryChanged() { inventory.onInventoryChanged(); }
	@Override public boolean isUseableByPlayer(EntityPlayer entityplayer) { return inventory.isUseableByPlayer(entityplayer); }
	@Override public void openInventory() { inventory.openInventory(); }
	@Override public void closeInventory() { inventory.closeInventory(); }
	@Override public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) { return inventory.isItemValidForSlot(slotIndex, itemstack); }

	/* STATIC HELPER */
	public static ISidedInventory getWrappedInventory(Object inventory) {
		if(inventory instanceof ISidedInventory)
			return (ISidedInventory)inventory;
		else if(inventory instanceof IInventory)
			return new InventoryWrapperSimple(InvUtils.getInventory((IInventory)inventory));
		else
			return null;
	}
}
