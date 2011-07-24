package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.IPowerReceptor;
import net.minecraft.src.buildcraft.core.Utils;

public class TileWoodenPipe extends TilePipe implements IPowerReceptor {
	
	long lastMining = 0;
	boolean lastPower = false;
	
	private PowerProvider powerProvider;
	
	public TileWoodenPipe () {
		powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
		powerProvider.configure(50, 1, 64, 1, 64);
		powerProvider.configurePowerPerdition(64, 1);
	}	
	/** 
	 * Extracts a random piece of item outside of a nearby chest.
	 */
	public void doWork () {
		if (powerProvider.energyStored <= 0) {
			return;
		}
		
		World w = worldObj;
		
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (meta > 5) {
			return;
		}
		
		Position pos = new Position(xCoord, yCoord, zCoord,
				Orientations.values()[meta]);		
		pos.moveForwards(1);
		int blockId = w.getBlockId((int) pos.x, (int) pos.y,
				(int) pos.z);
		TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y,
				(int) pos.z);					
		
		if (tile == null
				|| !(tile instanceof IInventory)
				|| BlockWoodenPipe
						.isExcludedFromExtraction(Block.blocksList[blockId])) {
			return;
		}
		
		IInventory inventory = (IInventory) tile;
		
		ItemStack stack = checkExtract(inventory, true,
				pos.orientation.reverse());	
		
		if (stack == null || stack.stackSize == 0) {
			return;
		}
		
		Position entityPos = new Position(pos.x + 0.5, pos.y
				+ Utils.getPipeFloorOf(stack), pos.z + 0.5,
				pos.orientation.reverse());
				
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
			// TAKE INTO ACCOUNT SPECIAL INVENTORIES!!!
			return ((ISpecialInventory) inventory).extractItem(doRemove, from);
		}
		
		if (inventory.getSizeInventory() == 2) {
			//  This is an input-output inventory
			
		    int slotIndex = 0;

		    if (from == Orientations.YNeg || from == Orientations.YPos) {
		        slotIndex = 0;
		    } else {
		        slotIndex = 1;
		    }

		    ItemStack slot = inventory.getStackInSlot(slotIndex);

		    if (slot != null && slot.stackSize > 0) {                       
		        if (doRemove) {
					return inventory.decrStackSize(slotIndex,
							powerProvider.useEnergy(1, slot.stackSize, true));
		        } else {
		            return slot;
		        }                   
		    }       
		} else if (inventory.getSizeInventory() == 3) {
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
					return inventory.decrStackSize(slotIndex,
							powerProvider.useEnergy(1, slot.stackSize, true));
				} else {
					return slot;
				}			
			}	
		} else {
			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);
			
			ItemStack result = checkExtractGeneric(inv, doRemove, from);
			
			if (result != null) {
				return result;
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
						return inventory.decrStackSize(k,
								powerProvider.useEnergy(1, slot.stackSize, true));
					} else {
						return slot;
					}
				}
			}
		}

		return null;
	}
	
	public void switchSource () {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int newMeta = 6;
		
		for (int i = meta + 1; i <= meta + 6; ++i) {
			Orientations o = Orientations.values() [i % 6];
			
			Position pos = new Position (xCoord, yCoord, zCoord, o);
			
			pos.moveForwards(1);
			
			Block block = Block.blocksList[worldObj.getBlockId((int) pos.x,
					(int) pos.y, (int) pos.z)];
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if (tile instanceof IInventory
					&& Utils.checkPipesConnections(worldObj, xCoord, yCoord,
							zCoord, tile.xCoord, tile.yCoord, tile.zCoord)) {
				if (!BlockWoodenPipe.isExcludedFromExtraction(block)) {
					newMeta = o.ordinal();
					break;
				}
			}
		}
		
		if (newMeta != meta) {
			worldObj.setBlockMetadata(xCoord, yCoord, zCoord, newMeta);
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	private boolean inSetSource = false;
	private boolean scheduleSourceSet = false;
	
	public void scheduleSourceSet () {
		scheduleSourceSet  = true;
	}
	
	private void setSourceIfNeeded () {			
		if (inSetSource) {
			//  This is needed to protect against corner recursive cases
			
			return;
		}		
		
		inSetSource = true;
		
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (meta > 5) {
			switchSource();
		} else {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[meta]);		
			pos.moveForwards(1);

			if (!(worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z) instanceof IInventory)) {

				switchSource();
			}
		}
				
		inSetSource = false;		
	}
	

	public void initialize () {
		super.initialize();
		
		setSourceIfNeeded();
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;		
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}
	
	@Override
	public void updateEntity () {
		super.updateEntity();
		
		if (scheduleSourceSet) {
			setSourceIfNeeded();
			scheduleSourceSet = false;
		}
	}
}
