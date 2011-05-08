package net.minecraft.src.buildcraft;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.BlockCloth;
import net.minecraft.src.BlockLeaves;
import net.minecraft.src.BlockLog;
import net.minecraft.src.BlockOre;
import net.minecraft.src.BlockSapling;
import net.minecraft.src.BlockStep;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class Utils {
	
	public static final float pipeMinSize = 0.25F;
	public static final float pipeMaxSize = 0.75F;
	public static float pipeNormalSpeed = 0.01F;
	
	/**
	 * Return True if the item id is supposed to be connected to the
	 * blockId given in parameter, for e.g. display purpose
	 */
	public static boolean isPipeConnected(IBlockAccess blockAccess, int i, int j, int k, int toBlockId) {
		int id = blockAccess.getBlockId(i, j, k);
		TileEntity tileEntity = blockAccess.getBlockTileEntity(i, j, k);
		
		if (toBlockId == mod_BuildCraft.getInstance().frameBlock.blockID) {
			 return id == mod_BuildCraft.getInstance().frameBlock.blockID;
		} else {
			return tileEntity instanceof IPipeEntry 
			|| tileEntity instanceof IInventory 				        
			|| id == mod_BuildCraft.getInstance().machineBlock.blockID				
			|| id == mod_BuildCraft.getInstance().miningWellBlock.blockID;
		}
	}

	
	/**
	 * Depending on the kind of item in the pipe, set the floor at a different
	 * level to optimize graphical aspect.
	 */
	public static float getPipeFloorOf (ItemStack item) {
		if (item.itemID < Block.blocksList.length) {
			return 0.4F;
		} else {
			return 0.27F;
		}
	}
	
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
	 * add the items to a random pipe entry around. Will make sure that the 
	 * location from which the items are coming from (identified by the from 
	 * parameter) isn't used again so that entities doesn't go backwards. 
	 * Returns true if successful, false otherwise.
	 */
	public static boolean addToRandomPipeEntry (TileEntity tile, Orientations from, ItemStack items) {
		World w = ModLoader.getMinecraftInstance().theWorld;
		
		LinkedList <Orientations> possiblePipes = new LinkedList <Orientations> ();
		
		for (int j = 0; j < 6; ++j) {
			if (from.reverse().ordinal() == j) {
				continue;
			}
			
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					Orientations.values()[j]);
			
			pos.moveForwards(1.0);
			
			TileEntity pipeEntry = w.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			if (pipeEntry instanceof IPipeEntry) {
				possiblePipes.add(Orientations.values()[j]);
			}
		}
		
		if (possiblePipes.size() > 0) {
			int choice = w.rand.nextInt(possiblePipes.size());
			
			Position entityPos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					possiblePipes.get(choice));
			Position pipePos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					possiblePipes.get(choice));
			
			entityPos.i += 0.5;
			entityPos.j += getPipeFloorOf(items);
			entityPos.k += 0.5;
			
			entityPos.moveForwards(0.5);
			
			pipePos.moveForwards(1.0);
			
			IPipeEntry pipeEntry = (IPipeEntry) w.getBlockTileEntity(
					(int) pipePos.i, (int) pipePos.j, (int) pipePos.k);
			
			EntityPassiveItem entity = new EntityPassiveItem(w, entityPos.i,
					entityPos.j, entityPos.k, items);
			
			w.entityJoinedWorld(entity);
			pipeEntry.entityEntering(entity, entityPos.orientation);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Look around the tile given in parameter in all 6 position, tries to
	 * add the items to a random inventory around. Will make sure that the location
	 * from which the items are coming from (identified by the from parameter)
	 * isn't used again so that entities doesn't go backwards. Returns true if
	 * successful, false otherwise.
	 */
	public static boolean addToRandomInventory (TileEntity tile, Orientations from, ItemStack items) {
		World w = ModLoader.getMinecraftInstance().theWorld;
		
		LinkedList <Orientations> possibleInventories = new LinkedList <Orientations> ();
		
		for (int j = 0; j < 6; ++j) {
			if (from.reverse().ordinal() == j) {
				continue;
			}
			
			Position pos = new Position(tile.xCoord, tile.yCoord, tile.zCoord,
					Orientations.values()[j]);
			
			pos.moveForwards(1.0);
			
			TileEntity tileInventory = w.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			if (tileInventory instanceof IInventory) {
				if (checkAvailableSlot((IInventory) tileInventory, items,
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
			
			TileEntity tileInventory = w.getBlockTileEntity((int) pos.i,
					(int) pos.j, (int) pos.k);
			
			checkAvailableSlot((IInventory) tileInventory, items, true,
					pos.orientation.reverse());
			
			return true;
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
	public static boolean checkAvailableSlot(IInventory inventory,
			ItemStack items, boolean add, Orientations from) {
		// First, look for a similar pile
		
		if (inventory.getSizeInventory() == 3) {
			//  This is a furnace-like inventory
			
			if (from == Orientations.YPos) {
				if (tryAdding (items, inventory, 0, add, false)) {
					return true;
				}
			} else if (from == Orientations.YNeg) {
				if (tryAdding (items, inventory, 1, add, false)) {
					return true;
				}
			}
			
		} else if (inventory.getSizeInventory() == 9) {
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
			
			if (tryAdding (items, inventory, minSlot, add, false)) {
				return true;
			}
		} else {
			//  This is a generic inventory
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				if (tryAdding (items, inventory, j, add, false)) {
					return true;
				}
			}
		}

		// If none, then create a new thing

		if (inventory.getSizeInventory() == 3) {
			//  This is a furnace-like inventory
			
			if (from == Orientations.YPos) {
				if (tryAdding (items, inventory, 0, add, true)) {
					return true;
				}
			} else if (from == Orientations.YNeg) {
				if (tryAdding (items, inventory, 1, add, true)) {
					return true;
				}
			}
			
		} else if (inventory.getSizeInventory() == 9) { 
			//  In the case of a workbench inventory, don't do anything
			
			return false;
		} else {
			//  This is a generic inventory
			
			for (int j = 0; j < inventory.getSizeInventory(); ++j) {
				if (tryAdding (items, inventory, j, add, true)) {
					return true;
				}
			}
		}

		// If the inventory if full, return false

		return false;
	}
	
	/**
	 * Try adding the items given in parameter in the inventory, at the given
	 * stackIndex. If doAdd is false, then no item will actually get added. If
	 * addInEmpty is true, then items will be added in empty slots only,
	 * otherwise in slot containing the same item only.
	 */
	public static boolean tryAdding(ItemStack items, IInventory inventory,
			int stackIndex, boolean doAdd, boolean addInEmpty)
	{
		ItemStack stack = inventory.getStackInSlot(stackIndex);

		if (!addInEmpty) {
			if (stack != null) {
				if (stack.getItem() == items.getItem()
						&& stack.stackSize + items.stackSize <= stack
						.getMaxStackSize()) {

					if (doAdd) {
						stack.stackSize += items.stackSize;
					}					

					return true;
				}
			}
		} else {
			if (stack == null) {
				if (doAdd) {
					stack = new ItemStack(items.itemID, items.stackSize,
							items.getItemDamage());
					inventory.setInventorySlotContents(stackIndex, stack);
				}

				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the tile only if it is of the proper type. Null otherwise.
	 */
	public static <T extends TileEntity> T getSafeTile (World w, int i, int j, int k, Class tileClass) {
		// TODO: Check if it's normal that the tile doesn't correspond...
    	TileEntity tileTest = w.getBlockTileEntity(i, j, k);

    	if (!(tileTest.getClass().isAssignableFrom(tileClass))) {
			System.out.println("   ERROR: Type of type is "
					+ tileTest.getClass() + " INSTEAD OF "
					+ tileClass);
    		
    		for (StackTraceElement e :  Thread.currentThread().getStackTrace()) {
    			System.out.println (e);
    		}
    		
    		return null;
    	}
    	
		return (T) tileTest;
	}
	
	public static int damageDropped (int blockId) {
		if (Block.blocksList [blockId] instanceof BlockCloth) {
			return blockId;
		} else if (Block.blocksList [blockId] instanceof BlockSapling) {
			return blockId & 3;
		} else if (Block.blocksList [blockId] instanceof BlockStep) {
			return blockId;
		} else if (Block.blocksList [blockId] instanceof BlockLeaves) {
			return blockId & 3;
		} else if (Block.blocksList [blockId] instanceof BlockLog) {
			return blockId;
		} else if (Block.blocksList [blockId] instanceof BlockOre) {
			return blockId != Block.oreLapis.blockID ? 0 : 4;
		}
			
		
		return 0;
	}
}
