package buildcraft.core.inventory;

import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorRoundRobin extends TransactorSimple {

	public TransactorRoundRobin(IInventory inventory) {
		super(inventory);
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {

		int added = 0;

		for (int itemLoop = 0; itemLoop < stack.stackSize; ++itemLoop) { // add 1 item n times.

			int smallestStackSize = Integer.MAX_VALUE;
			IInvSlot minSlot = null;

			for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
				ItemStack stackInInventory = slot.getStackInSlot();

				if (stackInInventory == null) {
					continue;
				}

				if (stackInInventory.stackSize >= stackInInventory.getMaxStackSize()) {
					continue;
				}

				if (stackInInventory.stackSize >= inventory.getInventoryStackLimit()) {
					continue;
				}

				if (StackHelper.instance().canStacksMerge(stack, stackInInventory) && stackInInventory.stackSize < smallestStackSize) {
					smallestStackSize = stackInInventory.stackSize;
					minSlot = slot;
				}
				if (smallestStackSize <= 1) {
					break;
				}
			}

			if (minSlot != null) {
				added += addToSlot(minSlot, stack, stack.stackSize - 1, doAdd); // add 1 item n times, into the selected slot
			} else {
				break;
			}

		}

		return added;
	}
}
