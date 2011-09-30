/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

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
	 * Tries to add items from. Return true if at least an object
	 * can be added, false otherwise. Addition is actually done only when doAdd 
	 * is true, otherwise it's just checking the possibility. When doAdd is
	 * true, stack.stackSize is updated.
	 * 
	 * from contains the side to which the addition request is made.
	 */
	public boolean addItem (ItemStack stack, boolean doAdd, Orientations from);
	
	/**
	 * Extract an item. Inventories implementing this function have their own
	 * algorithm to extract objects, e.g. to pipes.
	 * 
	 * If doRemove is false, then the returned stack will not actually remove
	 * object from the inventory, but just show what can be removed.
	 * 
	 * from contains the side to which the extraction request is made.
	 */
	public ItemStack extractItem(boolean doRemove, Orientations from);
	
}
