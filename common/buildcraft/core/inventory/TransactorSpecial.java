package buildcraft.core.inventory;

import net.minecraft.src.ItemStack;
import buildcraft.api.core.Orientations;
import buildcraft.api.inventory.ISpecialInventory;

public class TransactorSpecial extends Transactor {

	protected ISpecialInventory inventory;
	
	public TransactorSpecial(ISpecialInventory inventory) {
		this.inventory = inventory;
	}
	
	@Override
	public int inject(ItemStack stack, Orientations orientation, boolean doAdd) {
		return inventory.addItem(stack, doAdd, orientation);
	}
	
	@Override
	public int injectSpecific(ItemStack stack,int slot, Orientations orientation, boolean doAdd) {
		return inventory.addItem(stack, doAdd, orientation); // a special inventory knows its slots better than we do, trust that it gets it right.
	}
}
