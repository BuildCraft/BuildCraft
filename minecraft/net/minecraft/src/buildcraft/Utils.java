package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;

public class Utils {
	
	public static Orientations get2dOrientation (Position pos1, Position pos2) {
		double Dx = pos1.i - pos2.i;
    	double Dz = pos1.k - pos2.k;
    	double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;    	
    	
    	if (angle < 45 || angle > 315) {
    		return Orientations.XPos;
    	} else if (angle < 135) {
    		return Orientations.ZPos;
    	} else if (angle < 225) {
    		return Orientations.XNeg;
    	} else {
    		return Orientations.ZNeg;
    	}    	    	    	
	}	
	
	public static Orientations get3dOrientation (Position pos1, Position pos2) {
		double Dx = pos1.i - pos2.i;
    	double Dy = pos1.j - pos2.j;
    	double angle = Math.atan2(Dy, Dx) / Math.PI * 180 + 180;
    	
    	if (angle > 45 && angle < 135) {
    		return Orientations.YPos;
    	} else if (angle > 225 && angle < 315) {
    		return Orientations.YNeg;
    	} else {
    		return get2dOrientation(pos1, pos2);
    	}    	
	}
	
	/**
	 * Look around the tile given in parameter in all 6 position, tries to
	 * add the items to a random chest around. Will make sure that the location
	 * from which the items are coming from (identified by the from parameter)
	 * isn't used again so that entities doesn't go backwards. Returns true if
	 * successful, false otherwise.
	 */
	public static boolean addToRandomChest (TileEntity tile, Orientations from, ItemStack items) {
		World w = ModLoader.getMinecraftInstance().theWorld;
		
		LinkedList <TileEntityChest> possibleChests = new LinkedList <TileEntityChest> ();
		
		for (int j = 0; j < 6; ++j) {
			if (from.reverse().ordinal() == j) {
				continue;
			}
			
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					Orientations.values()[j]);
			
			pos.moveForwards(1.0);
			
			TileEntity tileChest = w.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			if (tileChest instanceof TileEntityChest) {
				if (checkAvailableSlot ((TileEntityChest) tileChest, items, false)) {
					possibleChests.add((TileEntityChest) tileChest);
				}
			}
		}
		
		if (possibleChests.size() > 0) {
			int choice = w.rand.nextInt(possibleChests.size());
			
			checkAvailableSlot(possibleChests.get(choice), items, true);
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if all the items can be added to the inventory. If add is
	 * true, they will be effectively added.
	 */
	public static boolean checkAvailableSlot(IInventory inventory,
			ItemStack items, boolean add) {
		// First, look for a similar pile

		for (int j = 0; j < inventory.getSizeInventory(); ++j) {
			ItemStack stack = inventory.getStackInSlot(j);
			if (stack != null) {
				if (stack.getItem() == items.getItem()
						&& stack.stackSize + items.stackSize <= stack.getMaxStackSize()) {
					
					if (add) {
						stack.stackSize += items.stackSize;
					}

					return true;
				}
			}
		}

		// If none, then create a new thing

		for (int j = 0; j < inventory.getSizeInventory(); ++j) {
			ItemStack stack = inventory.getStackInSlot(j);
			if (stack == null) {
				
				if (add) {
					stack = new ItemStack(items.getItem(), items.stackSize);
					inventory.setInventorySlotContents(j, stack);
				}

				return true;
			}
		}

		// If the chest if full, return false

		return false;
	}
}
