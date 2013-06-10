package buildcraft.core.inventory;

import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorSimple extends Transactor {

	protected static final StackMergeHelper MERGE_HELPER = new StackMergeHelper();
	protected IInventory inventory;

	public TransactorSimple(IInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		List<IInvSlot> filledSlots = new ArrayList<IInvSlot>(inventory.getSizeInventory());
		List<IInvSlot> emptySlots = new ArrayList<IInvSlot>(inventory.getSizeInventory());
		for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
			if (slot.canPutStackInSlot(stack)) {
				if (slot.getStackInSlot() == null) {
					emptySlots.add(slot);
				} else {
					filledSlots.add(slot);
				}
			}
		}

		int injected = 0;
		injected = tryPut(stack, filledSlots, injected, doAdd);
		injected = tryPut(stack, emptySlots, injected, doAdd);

		inventory.onInventoryChanged();
		return injected;
	}

	private int tryPut(ItemStack stack, List<IInvSlot> slots, int injected, boolean doAdd) {
		if (injected >= stack.stackSize) {
			return injected;
		}
		for (IInvSlot slot : slots) {
			ItemStack stackInSlot = slot.getStackInSlot();
			if (stackInSlot == null || MERGE_HELPER.canStacksMerge(stackInSlot, stack)) {
				int used = addToSlot(slot, stack, injected, doAdd);
				if (used > 0) {
					injected += used;
					if (injected >= stack.stackSize) {
						return injected;
					}
				}
			}
		}
		return injected;
	}

	/**
	 *
	 * @param slot
	 * @param stack
	 * @param injected Amount not to move?
	 * @param doAdd
	 * @return Return the number of items moved.
	 */
	protected int addToSlot(IInvSlot slot, ItemStack stack, int injected, boolean doAdd) {
		int available = stack.stackSize - injected;
		int max = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());

		ItemStack stackInSlot = slot.getStackInSlot();
		if (stackInSlot == null) {
			int wanted = Math.min(available, max);
			if (doAdd) {
				stackInSlot = stack.copy();
				stackInSlot.stackSize = wanted;
				slot.setStackInSlot(stackInSlot);
			}
			return wanted;
		}

		if (!MERGE_HELPER.canStacksMerge(stack, stackInSlot)) {
			return 0;
		}

		int wanted = max - stackInSlot.stackSize;
		if (wanted <= 0) {
			return 0;
		}

		if (wanted > available) {
			wanted = available;
		}

		if (doAdd) {
			stackInSlot.stackSize += wanted;
			slot.setStackInSlot(stackInSlot);
		}
		return wanted;
	}
}
