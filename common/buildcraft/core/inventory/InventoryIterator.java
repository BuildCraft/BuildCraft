/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import buildcraft.api.core.IInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class InventoryIterator {

	/**
	 * Returns an Iterable object for the specified side of the inventory.
	 *
	 * @param inv
	 * @param side
	 * @return Iterable
	 */
	public static Iterable<IInvSlot> getIterable(IInventory inv, ForgeDirection side) {
		if (inv instanceof ISidedInventory) {
			return new InventoryIteratorSided((ISidedInventory) inv, side);
		}

		return new InventoryIteratorSimple(inv);
	}

}
