/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
