package buildcraft.core.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUtil {

	private final IInventory _inventory;

	public InventoryUtil(IInventory inventory) {
		this._inventory = inventory;
	}

	public int roomForItem(ItemStack itemToTest) {
		if (itemToTest == null)
			return 0;

		int totalRoom = 0;
		for (int i = 0; i < _inventory.getSizeInventory(); i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) {
				totalRoom += Math.min(_inventory.getInventoryStackLimit(), itemToTest.getMaxStackSize());
				continue;
			}
			if (itemToTest.itemID != stack.itemID || (!itemToTest.getItem().isDamageable() && itemToTest.getItemDamage() != stack.getItemDamage())) {
				continue;
			}

			totalRoom += (Math.min(_inventory.getInventoryStackLimit(), itemToTest.getMaxStackSize()) - stack.stackSize);
		}
		return totalRoom;
	}

	public boolean hasRoomForItem(ItemStack itemToTest) {
		return roomForItem(itemToTest) > 0;
	}

	public int getIdForFirstSlot() {
		for (int i = 0; i < _inventory.getSizeInventory(); i++) {
			if (_inventory.getStackInSlot(i) != null)
				return i;
		}
		return -1;
	}

	public boolean isEmpty() {
		return (getIdForFirstSlot() >= 0);
	}

	public ItemStack addToInventory(ItemStack stackToMove) {
		for (int i = 0; i < _inventory.getSizeInventory(); i++) {
			ItemStack stack = _inventory.getStackInSlot(i);
			if (stack == null) {
				_inventory.setInventorySlotContents(i, stackToMove);
				return null;
			}
			if (stackToMove.itemID == stack.itemID && (stackToMove.getItem().isDamageable() || stackToMove.getItemDamage() == stack.getItemDamage())) {
				if (stackToMove.stackSize + stack.stackSize <= stack.getMaxStackSize()) {
					stack.stackSize += stackToMove.stackSize;
					return null;
				}
				int itemsToMove = stack.getMaxStackSize() - stack.stackSize;
				stack.stackSize += itemsToMove;
				stackToMove.stackSize -= itemsToMove;
			}
		}
		return stackToMove;
	}
}
