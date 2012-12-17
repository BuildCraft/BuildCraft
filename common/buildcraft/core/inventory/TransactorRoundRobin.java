package buildcraft.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorRoundRobin extends TransactorSimple {

	public TransactorRoundRobin(IInventory inventory) {
		super(inventory);
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {

		int oneLessThanStackSize = stack.stackSize - 1;
		int added = 0;

		for (int itemLoop = 0; itemLoop < stack.stackSize; ++itemLoop) { // add 1 item n times.

			int minSimilar = Integer.MAX_VALUE;
			int minSlot = -1;

			for (int j = 0; j < inventory.getSizeInventory() && minSimilar > 1; ++j) {
				ItemStack stackInInventory = inventory.getStackInSlot(j);

				if (stackInInventory == null) {
					continue;
				}

				if (stackInInventory.stackSize >= stackInInventory.getMaxStackSize()) {
					continue;
				}

				if (stackInInventory.stackSize >= inventory.getInventoryStackLimit()) {
					continue;
				}

				if (stackInInventory.stackSize > 0 && stackInInventory.itemID == stack.itemID && stackInInventory.getItemDamage() == stack.getItemDamage()
						&& stackInInventory.stackSize < minSimilar) {
					minSimilar = stackInInventory.stackSize;
					minSlot = j;
				}
			}

			if (minSlot != -1) {
				added += addToSlot(minSlot, stack, oneLessThanStackSize, doAdd); // add 1 item n times, into the selected slot
			} else {
				break;
			}

		}

		return added;
	}

}
