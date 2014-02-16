/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import buildcraft.core.inventory.filters.IStackFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

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
