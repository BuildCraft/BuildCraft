package buildcraft.core.inventory;

import buildcraft.api.core.Orientations;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.ISidedInventory;

public class TransactorSided extends TransactorSimple {

	ISidedInventory sided;
	
	public TransactorSided(ISidedInventory inventory) {
		super(inventory);
		this.sided = inventory;
	}

	@Override
	protected int getPartialSlot(ItemStack stack, Orientations orientation, int skipAhead) {
		
		if(skipAhead < sided.getStartInventorySide(orientation.toDirection())
				|| skipAhead > sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()))
			return -1;
		
		return getPartialSlot(stack, skipAhead, sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()));
	}
	
	@Override
	protected int getEmptySlot(Orientations orientation) {
		return getEmptySlot(sided.getStartInventorySide(orientation.toDirection()),
				sided.getStartInventorySide(orientation.toDirection()) + sided.getSizeInventorySide(orientation.toDirection()));
	}
	

}
