package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class TileWoodenPipe extends TilePipe {
	
	long lastMining = 0;
	boolean lastPower = false;
	
	public TileWoodenPipe () {
		latency = 50;
	}
	
	/** 
	 * Extracts a random piece of item outside of a nearby chest.
	 */
	public void doWork () {		
		World w = worldObj;
			
		LinkedList<Position> inventories = new LinkedList<Position>();
		
		for (int j = 0; j < 6; ++j) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[j]);
			pos.moveForwards(1.0);
			
			TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if (tile instanceof IInventory) {
				IInventory inventory = (IInventory) tile;
				
				if (checkExtract(inventory, false, pos.orientation.reverse()) != null) {
					inventories.add(pos);
				}
			}
		}
		
		if (inventories.size() == 0) {
			return;
		}
		
		Position chestPos = inventories.get(w.rand.nextInt(inventories.size()));
		IInventory inventory = (IInventory) w.getBlockTileEntity(
				(int) chestPos.x, (int) chestPos.y, (int) chestPos.z);
		
		ItemStack stack = checkExtract(inventory, true,
				chestPos.orientation.reverse());								
		
		Position entityPos = new Position(chestPos.x + 0.5, chestPos.y
				+ Utils.getPipeFloorOf(stack), chestPos.z + 0.5,
				chestPos.orientation.reverse());
				
		entityPos.moveForwards(0.5);
		
		EntityPassiveItem entity = new EntityPassiveItem(w, entityPos.x,
				entityPos.y, entityPos.z, stack);
		
		w.entityJoinedWorld(entity);
		entityEntering(entity, entityPos.orientation);		
	}
	
	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	public ItemStack checkExtract (IInventory inventory, boolean doRemove, Orientations from) {
		if (inventory instanceof ISpecialInventory) {
			return ((ISpecialInventory) inventory).extractItemToPipe(doRemove, from);
		}
		
		if (inventory.getSizeInventory() == 3) {
			//  This is a furnace-like inventory
			
			int slotIndex = 0;
			
			if (from == Orientations.YPos) {
				slotIndex = 0;
			} else if (from == Orientations.YNeg) {
				slotIndex = 1;
			} else {
				slotIndex = 2;
			}
			
			ItemStack slot = inventory.getStackInSlot(slotIndex);
			
			if (slot != null && slot.stackSize > 0) {			
				if (doRemove) {
					return inventory.decrStackSize(slotIndex, 1);
				} else {
					return slot;
				}			
			}	
		} else {
			// This is a generic inventory
			
			ItemStack result = checkExtractGeneric(inventory, doRemove, from);
			
			if (result != null) {
				return result;
			}
			
			if (inventory instanceof TileEntityChest) {
				// If we're on a entity chest, check if there's an other chest
				// around
								
				TileEntityChest chest = Utils
						.getNearbyChest((TileEntityChest) inventory);
				
				if (chest != null) {
					return checkExtractGeneric((IInventory) chest, doRemove,
							from);
				}
			}
			
		}		
		
		return null;
	}
	
	public ItemStack checkExtractGeneric(IInventory inventory,
			boolean doRemove, Orientations from) {
		for (int k = 0; k < inventory.getSizeInventory(); ++k) {
			if (inventory.getStackInSlot(k) != null
					&& inventory.getStackInSlot(k).stackSize > 0) {

				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0) {
					if (doRemove) {
						return inventory.decrStackSize(k, 1);
					} else {
						return slot;
					}
				}
			}
		}

		return null;
	}

}
