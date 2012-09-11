package buildcraft.core.inventory;

import buildcraft.api.core.Orientations;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.ISidedInventory;

/**
 *	Will respect ISidedInventory implementation but only accept input from above or below. 
 */
public class TransactorFurnace extends TransactorSided {

	public TransactorFurnace(ISidedInventory inventory) {
		super(inventory);
	}

	@Override
	public int inject(ItemStack stack, Orientations orientation, boolean doAdd) {
		if(orientation != Orientations.YNeg
				&& orientation != Orientations.YPos)
			return 0;
		
		return super.inject(stack, orientation, doAdd);
	}
}
