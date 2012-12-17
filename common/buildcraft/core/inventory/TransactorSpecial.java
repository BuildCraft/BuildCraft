package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;

public class TransactorSpecial extends Transactor {

	protected ISpecialInventory inventory;

	public TransactorSpecial(ISpecialInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		return inventory.addItem(stack, doAdd, orientation);
	}

}
