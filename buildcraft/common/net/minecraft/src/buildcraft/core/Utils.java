package net.minecraft.src.buildcraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class Utils {
	
	private static Properties props = new Properties();
	
	public static final float pipeMinSize = 0.25F;
	public static final float pipeMaxSize = 0.75F;
	public static float pipeNormalSpeed = 0.01F;
	
	private static final File cfgfile = CoreProxy.getPropertyFile();
	
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
		World w = CoreProxy.getWorld();
		
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
		World w = CoreProxy.getWorld();
		
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
	
	public static void dropItems (World world, ItemStack stack, int i, int j, int k) {
		float f1 = 0.7F;
		double d = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d1 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d2 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, (double) i + d,
				(double) j + d1, (double) k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		
		world.entityJoinedWorld(entityitem);
	}
	
	public static void dropItems (World world, IInventory inventory, int i, int j, int k) {
		for (int l = 0; l < inventory.getSizeInventory(); ++l) {
			ItemStack items = inventory.getStackInSlot(l);
			
			if (items != null && items.stackSize > 0) {
				dropItems (world, inventory.getStackInSlot(l).copy(), i, j, k);
			}
    	}
	}
	
	public static void loadProperties () {
		try {
			cfgfile.getParentFile().mkdirs();

			if (!cfgfile.exists() && !cfgfile.createNewFile()) {
				return;
			}
			if (cfgfile.canRead()) {
				FileInputStream fileinputstream = new FileInputStream(cfgfile);
				props.load(fileinputstream);
				fileinputstream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

    public static void saveProperties() {
		try {
			cfgfile.getParentFile().mkdirs();
			if (!cfgfile.exists() && !cfgfile.createNewFile()) {
				return;
			}
			if (cfgfile.canWrite()) {
				FileOutputStream fileoutputstream = new FileOutputStream(
						cfgfile);
				props.store(fileoutputstream, "BuildCraft Config");
				fileoutputstream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String getProperty (String name, String defaultValue) {
    	if (props.getProperty(name, "deadbeef").equals("deadbeef")) {
    		props.setProperty(name, defaultValue);
    	}
    	
    	return props.getProperty(name);
    }
}
