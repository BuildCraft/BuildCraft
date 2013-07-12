package buildcraft.core.inventory;

import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class TransactorSpecial extends Transactor {

	protected ISpecialInventory inventory;

	public TransactorSpecial(ISpecialInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		return inventory.addItem(stack, doAdd, orientation);
	}

	@Override
	public ItemStack remove(IStackFilter filter, ForgeDirection orientation, boolean doRemove) {
		ItemStack[] extracted = inventory.extractItem(false, orientation, 1);
		if (extracted != null && extracted.length > 0 && filter.matches(extracted[0])) {
			if (doRemove) {
				inventory.extractItem(true, orientation, 1);
			}
			return extracted[0];
		}
		return null;
	}
}
