package buildcraft.core.inventory;

import buildcraft.api.core.Orientations;
import net.minecraft.src.ItemStack;
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
	protected int getPartialSlot(ItemStack stack, Orientations orientation, int skipAhead) {
		
		// If skipAhead is below the minimum required, we skip ahead to the first valid slot.
		if(skipAhead < sided.getStartInventorySide(orientation.toDirection()))
			skipAhead = sided.getStartInventorySide(orientation.toDirection());
			
		if(skipAhead > sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()))
			return -1;
		
		return getPartialSlot(stack, skipAhead, sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()));
	}
	
	@Override
	protected int getEmptySlot(Orientations orientation) {
		return getEmptySlot(sided.getStartInventorySide(orientation.toDirection()),
				sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()));
	}
	

}
