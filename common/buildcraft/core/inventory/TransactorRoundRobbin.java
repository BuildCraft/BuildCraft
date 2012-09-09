package buildcraft.core.inventory;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import buildcraft.api.core.Orientations;

public class TransactorRoundRobbin extends TransactorSimple {

	private IInventory myInventory;
	public TransactorRoundRobbin(IInventory iInventory) {
		super(iInventory);
		myInventory = iInventory;
	}
	@Override
	public int inject(ItemStack stack, Orientations orientation, boolean doAdd) {
		int oneLessThanStackSize = stack.stackSize - 1;
		int added = 0;
		for (int itemLoop = 0; itemLoop<stack.stackSize; ++itemLoop){ // add 1 item n times.
			int minSimilar = Integer.MAX_VALUE;
			int minSlot = -1;
			for (int j = 0; j < myInventory.getSizeInventory() && minSlot > 1; ++j) {
				ItemStack stackInInventory = myInventory.getStackInSlot(j);
	
				if (stackInInventory != null && stackInInventory.stackSize > 0 && stackInInventory.itemID == stack.itemID
						&& stackInInventory.getItemDamage() == stack.getItemDamage() && stackInInventory.stackSize < minSimilar) {
					minSimilar = stackInInventory.stackSize;
					minSlot = j;
				}
			}
	
			if (minSlot != -1) {
				added += addToSlot(minSlot, stack, oneLessThanStackSize, doAdd); // add 1 item n times, into the selected slot				
			} else // nowhere to add this
				break;
		}
		return added;
	}

}
