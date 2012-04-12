/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.forge.ISidedInventory;

public class StackUtil {

	public ItemStack items;

	public StackUtil (ItemStack stack) {
		this.items = stack;
	}
	
	/**
	 * Look around the tile given in parameter in all 6 position, tries to
	 * add the items to a random inventory around. Will make sure that the location
	 * from which the items are coming from (identified by the from parameter)
	 * isn't used again so that entities doesn't go backwards. Returns true if
	 * successful, false otherwise.
	 */
	public boolean addToRandomInventory (TileEntity tile, Orientations from) {
		World w = APIProxy.getWorld();
		
		LinkedList <Orientations> possibleInventories = new LinkedList <Orientations> ();
		
		for (int j = 0; j < 6; ++j) {
			if (from.reverse().ordinal() == j) {
				continue;
			}
			
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					Orientations.values()[j]);
			
			pos.moveForwards(1.0);
			
			TileEntity tileInventory = w.getBlockTileEntity((int) pos.x,
					(int) pos.y, (int) pos.z);
			
			if (tileInventory instanceof ISpecialInventory) {
				if (((ISpecialInventory) tileInventory).addItem(items,
						false, from)) {
					possibleInventories.add(pos.orientation);
				}
			}
			
			if (tileInventory instanceof IInventory) {
				if (Utils.checkPipesConnections(tile.worldObj, tile.xCoord,
						tile.yCoord, tile.zCoord, tileInventory.xCoord,
						tileInventory.yCoord, tileInventory.zCoord)
						&& checkAvailableSlot((IInventory) tileInventory,
								false, pos.orientation.reverse())) {
					possibleInventories.add(pos.orientation);
				}
			}
		}
		
		if (possibleInventories.size() > 0) {
			int choice = w.rand.nextInt(possibleInventories.size());
			
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					possibleInventories.get(choice));
			
			pos.moveForwards(1.0);
			
			TileEntity tileInventory = w.getBlockTileEntity((int) pos.x,
					(int) pos.y, (int) pos.z);
			
			checkAvailableSlot((IInventory) tileInventory, true,
					pos.orientation.reverse());
			
			if (items.stackSize > 0) {
				return addToRandomInventory(tileInventory, from);
			} else {			
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Checks if all the items can be added to the inventory. If add is
	 * true, they will be effectively added. Orientations is the direction to
	 * look to find the item, e.g. if the item is coming from the top, it
	 * will be YPos.
	 */
	public boolean checkAvailableSlot(IInventory inventory, boolean add,
			Orientations from) {
		// First, look for a similar pile
		
		if (inventory instanceof ISpecialInventory) {
			return ((ISpecialInventory) inventory).addItem(items, add,
					from);
		}
		
		boolean added = false;
		
		if (inventory instanceof ISidedInventory) {
			IInventory inv = Utils.getInventory(inventory);
			ISidedInventory sidedInv = (ISidedInventory) inv;
			
			int first = sidedInv.getStartInventorySide(from.ordinal());
			int last = first + sidedInv.getSizeInventorySide(from.ordinal()) - 1;
						
			for (int j = first; j <= last; ++j) {
				if (tryAdding (inv, j, add, false)) {
					added = true;
					break;
				}
			}
		} else if (inventory.getSizeInventory() == 2) {
   		   //  This is an input / output inventory
			
			if (from == Orientations.YNeg || from == Orientations.YPos) {
				if (tryAdding (inventory, 0, add, false)) {
					added = true;
				}
			} else {
				if (tryAdding (inventory, 1, add, false)) {
					added = true;
				}
			}  
		} else if (inventory.getSizeInventory() == 3) {
			//  This is a furnace-like inventory
			
			if (from == Orientations.YPos) {
				if (tryAdding (inventory, 0, add, false)) {
					added = true;
				}
			} else if (from == Orientations.YNeg) {
				if (tryAdding (inventory, 1, add, false)) {
					added = true;
				}
			}
			
		} else {
			//  This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);
			
			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				if (tryAdding (inv, j, add, false)) {
					added = true;
					break;
				}
			}
		}

		if (added) {
			if (!add) {
				return true;
			} else if (items.stackSize == 0) {
				return true;
			} else {
				checkAvailableSlot(inventory, added, from);
				
				return true;
			}
		}		
		
		// If none, then create a new thing

		if (inventory instanceof ISidedInventory) {
			IInventory inv = Utils.getInventory(inventory);
			ISidedInventory sidedInv = (ISidedInventory) inv;
			
			int first = sidedInv.getStartInventorySide(from.ordinal());
			int last = first + sidedInv.getSizeInventorySide(from.ordinal()) - 1;
			
			for (int j = first; j <= last; ++j) {
				if (tryAdding (inv, j, add, true)) {
					added = true;
					break;
				}
			}
		} else if (inventory.getSizeInventory() == 2) {
			//  This is an input / output inventory

			if (from == Orientations.YNeg || from == Orientations.YPos) {
				if (tryAdding (inventory, 0, add, true)) {
					added = true;
				}
			} else {
				if (tryAdding (inventory, 1, add, true)) {
					added = true;
				}
			}  
		} else if (inventory.getSizeInventory() == 3) {
			//  This is a furnace-like inventory

			if (from == Orientations.YPos) {
				if (tryAdding (inventory, 0, add, true)) {
					added = true;
				}
			} else if (from == Orientations.YNeg) {
				if (tryAdding (inventory, 1, add, true)) {
					added = true;
				}
			}
			
		} else {
			//  This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);
			
			for (int j = 0; j < inv.getSizeInventory(); ++j) {
				if (tryAdding (inv, j, add, true)) {
					added = true;
					break;
				}
			}
		}

		// If the inventory if full, return false

		if (added) {
			if (!add) {
				return true;
			} else if (items.stackSize == 0) {
				return true;
			} else {
				checkAvailableSlot(inventory, added, from);
				return true;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Try adding the items given in parameter in the inventory, at the given
	 * stackIndex. If doAdd is false, then no item will actually get added. If
	 * addInEmpty is true, then items will be added in empty slots only,
	 * otherwise in slot containing the same item only.
	 * 
	 * This will add one item at a time, and decrease the items member.
	 */
	public boolean tryAdding(IInventory inventory,
			int stackIndex, boolean doAdd, boolean addInEmpty)
	{
		ItemStack stack = inventory.getStackInSlot(stackIndex);

		if (!addInEmpty) {
			if (stack != null) {
				if (stack.getItem() == items.getItem()
						&& stack.getItemDamage() == items.getItemDamage()
						&& stack.stackSize + 1 <= stack
						.getMaxStackSize()) {

					if (doAdd) {
						stack.stackSize++;
						items.stackSize--;
					}					

					return true;
				}
			}
		} else {
			if (stack == null) {
				if (doAdd) {
					stack = items.copy();
					stack.stackSize = 1;
					
					items.stackSize--;
					inventory.setInventorySlotContents(stackIndex, stack);
				}

				return true;
			}
		}
		
		return false;
	}
	
	
}
