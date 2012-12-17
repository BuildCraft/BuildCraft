package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

/**
 * Will respect ISidedInventory implementation but only accept input from above or below.
 */
public class TransactorFurnace extends TransactorSided {

	public TransactorFurnace(ISidedInventory inventory) {
		super(inventory);
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		if (orientation != ForgeDirection.DOWN && orientation != ForgeDirection.UP)
			return 0;

		return super.inject(stack, orientation, doAdd);
	}
}
