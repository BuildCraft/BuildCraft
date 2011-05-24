package net.minecraft.src.buildcraft.api;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

/**
 * Pipes are able to get object in and out tile entities implementing inventory.
 * Inventories implementing this interface will allow finer control over what
 * can be added  / extracted and how.
 */
public interface ISpecialInventory extends IInventory {

	/**
	 * Tries to add items from the pipe. Return true if at least an object
	 * can be added, false otherwise. Addition is actually done only when doAdd 
	 * is true, otherwise it's just checking the possibility. When doAdd is
	 * true, stack.stackSize is updated. 
	 */
	public boolean addItemFromPipe (ItemStack stack, boolean doAdd);
	
	/**
	 * Extract an item to pipe. Inventories implementing this function have a
	 * special algorithm to extract objects to pipes.
	 * 
	 * If doRemove is false, then the returned stack will not actually remove
	 * object from the inventory, but just show what can be removed.
	 */
	public ItemStack extractItemToPipe(boolean doRemove);
	
}
