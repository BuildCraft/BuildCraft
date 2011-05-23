package net.minecraft.src.buildcraft.core;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IAutomaticWorkbench;
import net.minecraft.src.buildcraft.api.IPipeIgnoreInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;

public class StackUtil {

	private ItemStack items;

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
		World w = CoreProxy.getWorld();
		
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
			
			if (tileInventory instanceof IInventory
					&& !(tileInventory instanceof IPipeIgnoreInventory)) {
				if (checkAvailableSlot((IInventory) tileInventory,
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
		
		boolean added = false;
		
		if (inventory.getSizeInventory() == 3) {
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
			
		} else if (inventory instanceof IAutomaticWorkbench) {
			//  This is a workbench inventory. Try to add to the smallest slot
			//  that contains the expected item.
			
			int minSimilar = Integer.MAX_VALUE;
			int minSlot = 0;
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				ItemStack stack = inventory.getStackInSlot(j);
				
				if (stack != null && stack.stackSize > 0
						&& stack.itemID == items.itemID
						&& stack.getItemDamage() == items.getItemDamage()
						&& stack.stackSize < minSimilar) {
					minSimilar = stack.stackSize;
					minSlot = j;
				}								
			}
			
			if (tryAdding (inventory, minSlot, add, false)) {
				added = true;
			}
		} else {
			//  This is a generic inventory
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				if (tryAdding (inventory, j, add, false)) {
					added = true;
					break;
				}
			}
			
			if (!added && inventory instanceof TileEntityChest) {				
				TileEntityChest chest = Utils
						.getNearbyChest((TileEntityChest) inventory);
				
				if (chest != null) {
					for (int j = 0; j < chest.getSizeInventory(); ++j) {
						if (tryAdding (chest, j, add, false)) {
							added = true;
							break;
						}
					}
				}
			}
		}

		if (added) {
			if (!add) {
				return true;
			} else if (items.stackSize == 0) {
				return true;
			} else {
				return checkAvailableSlot(inventory, added, from);
			}
		}
		
		// If none, then create a new thing

		if (inventory.getSizeInventory() == 3) {
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
			
		} else if (inventory instanceof IAutomaticWorkbench) {
			//  In the case of a workbench inventory, don't do anything
			
			return false;
		} else {
			//  This is a generic inventory
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				if (tryAdding (inventory, j, add, true)) {
					added = true;
					break;
				}
			}
			
			if (!added && inventory instanceof TileEntityChest) {			
				TileEntityChest chest = Utils
						.getNearbyChest((TileEntityChest) inventory);
				
				if (chest != null) {
					for (int j = 0; j < chest.getSizeInventory(); ++j) {
						if (tryAdding (chest, j, add, true)) {
							added = true;
							break;
						}
					}
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
				return checkAvailableSlot(inventory, added, from);
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
	private boolean tryAdding(IInventory inventory,
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
					stack = new ItemStack(items.itemID, 1,
							items.getItemDamage());
					items.stackSize--;
					inventory.setInventorySlotContents(stackIndex, stack);
				}

				return true;
			}
		}
		
		return false;
	}
	
	
}
