package buildcraft.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorSimple extends Transactor {

	protected IInventory inventory;

	public TransactorSimple(IInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {

		int injected = 0;

		int slot = 0;
		while ((slot = getPartialSlot(stack, orientation, slot)) >= 0 && injected < stack.stackSize) {
			injected += addToSlot(slot, stack, injected, doAdd);
		}

		slot = 0;
		while ((slot = getEmptySlot(orientation)) >= 0 && injected < stack.stackSize) {
			injected += addToSlot(slot, stack, injected, doAdd);
		}
		inventory.onInventoryChanged();
		return injected;
	}

	protected int getPartialSlot(ItemStack stack, ForgeDirection orientation, int skipAhead) {
		return getPartialSlot(stack, skipAhead, inventory.getSizeInventory());
	}

	protected int getPartialSlot(ItemStack stack, int startSlot, int endSlot) {

		for (int i = startSlot; i < endSlot; i++) {
			if (inventory.getStackInSlot(i) == null) {
				continue;
			}

			if (!inventory.getStackInSlot(i).isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(i), stack)) {
				continue;
			}

			if (inventory.getStackInSlot(i).stackSize >= inventory.getStackInSlot(i).getMaxStackSize() || inventory.getStackInSlot(i).stackSize >= inventory.getInventoryStackLimit()) {
				continue;
			}

			return i;
		}

		return -1;
	}

	protected int getEmptySlot(ForgeDirection orientation) {
		return getEmptySlot(0, inventory.getSizeInventory());
	}

	protected int getEmptySlot(int startSlot, int endSlot) {
		for (int i = startSlot; i < endSlot; i++)
			if (inventory.getStackInSlot(i) == null)
				return i;

		return -1;
	}

	protected int addToSlot(int slot, ItemStack stack, int injected, boolean doAdd) {
		int available = stack.stackSize - injected;
		int max = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());

		ItemStack stackInSlot = inventory.getStackInSlot(slot);
		if (stackInSlot == null) {
			int wanted = Math.min(available, max);
			if (doAdd) {
				stackInSlot = stack.copy();
				stackInSlot.stackSize = wanted;
				inventory.setInventorySlotContents(slot, stackInSlot);
			}
			return wanted;
		}

		if (!stackInSlot.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(stackInSlot, stack))
			return 0;

		int wanted = max - stackInSlot.stackSize;
		if (wanted <= 0)
			return 0;

		if (wanted > available)
			wanted = available;

		if (doAdd) {
			stackInSlot.stackSize += wanted;
			inventory.setInventorySlotContents(slot, stackInSlot);
		}
		return wanted;
	}
}
