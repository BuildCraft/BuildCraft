package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

/**
 * Manages input on ISidedInventory
 */
public class TransactorSided extends TransactorSimple {

	ISidedInventory sided;

	public TransactorSided(ISidedInventory inventory) {
		super(inventory);
		this.sided = inventory;
	}

	@Override
	protected int getPartialSlot(ItemStack stack, ForgeDirection orientation, int skipAhead) {

		// If skipAhead is below the minimum required, we skip ahead to the first valid slot.
		if (skipAhead < sided.getStartInventorySide(orientation)) {
			skipAhead = sided.getStartInventorySide(orientation);
		}

		if (skipAhead > sided.getStartInventorySide(orientation) + sided.getSizeInventorySide(orientation))
			return -1;

		return getPartialSlot(stack, skipAhead, sided.getStartInventorySide(orientation) + sided.getSizeInventorySide(orientation));
	}

	@Override
	protected int getEmptySlot(ForgeDirection orientation) {
		return getEmptySlot(sided.getStartInventorySide(orientation), sided.getStartInventorySide(orientation) + sided.getSizeInventorySide(orientation));
	}

}
