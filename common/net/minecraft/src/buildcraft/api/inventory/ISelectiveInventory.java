package net.minecraft.src.buildcraft.api.inventory;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.Orientations;

public interface ISelectiveInventory extends ISpecialInventory {
	/**
	 * Requests specified items to be extracted from the inventory
	 * @param desired Array which can contain ItemStacks, Items, or classes describing the type of item accepted or excluded.
	 * @param exclusion If true desired items are not eligible for returning.
	 * @param doRemove If false no actual extraction may occur.
	 * @param from Orientation the ItemStack is requested from.
	 * @param maxItemCount Maximum amount of items to extract (spread over all returned item stacks) 
	 * @return Array of item stacks extracted from the inventory
	 */
	ItemStack[] extractItem(Object[] desired, boolean exclusion, boolean doRemove, Orientations from, int maxItemCount);
}
