package buildcraft.core.inventory;

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
}
