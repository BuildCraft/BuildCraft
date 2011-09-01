package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.TileNetworkData;
import net.minecraft.src.buildcraft.core.Utils;

public class TileWoodenPipe extends TilePipe implements IPowerReceptor {
	
	long lastMining = 0;
	boolean lastPower = false;
	
	private PowerProvider powerProvider;
	
	public @TileNetworkData int liquidToExtract;
	
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
				|| !(tile instanceof IInventory || tile instanceof ILiquidContainer)
				|| BlockWoodenPipe
						.isExcludedFromExtraction(Block.blocksList[blockId])) {
			return;
		}
		
		
		if (tile instanceof IInventory) {
			IInventory inventory = (IInventory) tile;

			ItemStack stack = checkExtract(inventory, true,
					pos.orientation.reverse());	

			if (stack == null || stack.stackSize == 0) {			
				powerProvider.useEnergy(1, 1, false);
				return;
			}


			Position entityPos = new Position(pos.x + 0.5, pos.y
					+ Utils.getPipeFloorOf(stack), pos.z + 0.5,
					pos.orientation.reverse());

			entityPos.moveForwards(0.5);

			EntityPassiveItem entity = new EntityPassiveItem(w, entityPos.x,
					entityPos.y, entityPos.z, stack);

			entityEntering(entity, entityPos.orientation);
		} else if (tile instanceof ILiquidContainer) {
			if (liquidToExtract <= BuildCraftCore.BUCKET_VOLUME) {
				liquidToExtract += powerProvider.useEnergy(1, 1, true)
						* BuildCraftCore.BUCKET_VOLUME;
				
				sendNetworkUpdate();
			}
		}
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
			
			if ((tile instanceof IInventory || tile instanceof ILiquidContainer
					&& !(tile instanceof TilePipe))
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
	
	@Override
	protected void neighborChange () {		
		super.neighborChange();
		
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
	}
	

	public void initialize () {
		super.initialize();
		
		scheduleNeighborChange();
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
		
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (liquidToExtract > 0 && meta < 6) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[meta]);		
			pos.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);			
			
			if (tile instanceof ILiquidContainer) {
				ILiquidContainer container = (ILiquidContainer) tile;
				
				int extracted = container.empty(liquidToExtract > flowRate ? flowRate
						: liquidToExtract, false); 
				
				extracted = fill(pos.orientation, extracted, container.getLiquidId());
				
				container.empty(extracted, true);
				
				liquidToExtract -= extracted;
			}
		}
	}
}
