package buildcraft.core.inventory;

import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public interface ITransactor {

	/**
	 * Adds an Item to the inventory.
	 *
	 * @param stack
	 * @param orientation
	 * @param doAdd
	 * @return The ItemStack, with stackSize equal to amount moved.
	 */
	ItemStack add(ItemStack stack, ForgeDirection orientation, boolean doAdd);

	/**
	 * Removes and returns a single item from the inventory matching the filter.
	 * 
	 * @param filter
	 * @param orientation
	 * @param doRemove
	 * @return 
	 */
	ItemStack remove(IStackFilter filter, ForgeDirection orientation, boolean doRemove);
}
