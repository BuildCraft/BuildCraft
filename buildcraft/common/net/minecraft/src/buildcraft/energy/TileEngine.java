package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.IPowerReceptor;

public class TileEngine extends TileEntity implements IPowerReceptor, ISpecialInventory {

	boolean init = false;
	
	EntityEngine entity;
	
	boolean lastPower = false;
	
	int progressPart = 0;

	public int orientation;
	
	private ItemStack itemInInventory;
	public int burnTime = 0;
	public int totalBurnTime = 0;
	
	public void switchPower () {
		boolean power = worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord);
	
		if (power != lastPower)	{
			lastPower = power;
			
			if (power) {
				entity.addEnergy(1);
			}
		}
	}
	
	@Override
	public void updateEntity () {
		if (!init) {
			if (entity == null) {
				int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
				
				if (kind == 0) {
					entity = new EntityEngineWood(worldObj);
				} else if (kind == 1) {
					entity = new EntityEngineStone(worldObj);
				} else if (kind == 2) {
					entity = new EntityEngineIron(worldObj);
				}
			}
			
			entity.orientation = Orientations.values()[orientation];
			
			entity.setPosition(xCoord, yCoord, zCoord);
			
			worldObj.entityJoinedWorld(entity);
			
			init = true;
		}
		
		if (progressPart != 0) {
			switch (entity.getEnergyStage()) {
			case Blue:
				entity.progress += 0.01;
				break;
			case Green:
				entity.progress += 0.04;
				break;
			case Yellow:
				entity.progress += 0.08;
				break;
			case Red:
				entity.progress += 0.16;
				break;
			}
			
			if (entity.progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				
				Position pos = new Position(xCoord, yCoord, zCoord,
						entity.orientation);
				pos.moveForwards(1.0);
				TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
						(int) pos.z);
				
				if (tile instanceof IPowerReceptor) {
					IPowerReceptor receptor = (IPowerReceptor) tile;
					
					int minEnergy = receptor.minEnergyExpected();
					
					if (minEnergy != -1 && minEnergy <= entity.energy) {
						int energySent = receptor.maxEnergyExpected();
						
						if (entity.energy < energySent) {
							energySent = entity.energy;
						}
						
						entity.energy -= energySent;
						receptor.receiveEnergy(energySent);
					}
				}
			} else if (entity.progress >= 1) {
				entity.progress = 0;
				progressPart = 0;
			}
		} else {
			Position pos = new Position(xCoord, yCoord, zCoord,
					entity.orientation);
			pos.moveForwards(1.0);
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);

			if (tile instanceof IPowerReceptor) {
				IPowerReceptor receptor = (IPowerReceptor) tile;
				int minEnergy = receptor.minEnergyExpected();

				if (minEnergy != -1 && minEnergy <= entity.energy) {
					progressPart = 1;										
				}
			}
		}

		if (entity instanceof EntityEngineStone) {
			if(burnTime > 0) {
				burnTime--;
				entity.addEnergy(1);
			}

			if (burnTime == 0) {
				burnTime = totalBurnTime = getItemBurnTime(itemInInventory);
				if (burnTime > 0) {
					decrStackSize(1, 1);				
				}
			}
		} else if (entity instanceof EntityEngineIron) {
			if(burnTime > 0) {
				burnTime--;
				entity.addEnergy(2);
			}
			
			if (itemInInventory != null
					&& itemInInventory.itemID == BuildCraftEnergy.bucketOil.shiftedIndex) {

				totalBurnTime = 100000;
				int stepTime = totalBurnTime / 10;
				
				if (burnTime + stepTime <= totalBurnTime) {
					itemInInventory = new ItemStack(Item.bucketEmpty, 1);
					burnTime = burnTime + stepTime;
				}
			}
		}
	}
	
	public void switchOrientation () {						
		for (int i = orientation + 1; i <= orientation + 6; ++i) {
			Orientations o = Orientations.values() [i % 6];
			
			Position pos = new Position (xCoord, yCoord, zCoord, o);
			
			pos.moveForwards(1);
			
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if (tile instanceof IPowerReceptor) {
				if (entity != null) {
					entity.orientation = o;	
				}				
				orientation = o.ordinal();
				break;
			}
		}
	}
	
	public void delete () {
		entity.setEntityDead();
	}
	
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
    	super.readFromNBT(nbttagcompound);
    	
		int kind = nbttagcompound.getInteger("kind");
		
		if (kind == 0) {
			entity = new EntityEngineWood(APIProxy.getWorld());
		} else if (kind == 1) {
			entity = new EntityEngineStone(APIProxy.getWorld());
		} else if (kind == 2) {
			entity = new EntityEngineIron(APIProxy.getWorld());
		}
		
		orientation = nbttagcompound.getInteger("orientation");
    	entity.progress = nbttagcompound.getFloat("progress");
    	entity.energy = nbttagcompound.getInteger("energy");
    	entity.orientation = Orientations.values()[orientation];
    	totalBurnTime = nbttagcompound.getInteger("totalBurnTime");
    	burnTime = nbttagcompound.getInteger("burnTime");
    	
    	if (nbttagcompound.hasKey("itemInInventory")) {
    		NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
    		itemInInventory = new ItemStack(cpt);
    	}
    }
    

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	
		nbttagcompound.setInteger("kind",
				worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
		nbttagcompound.setInteger("orientation", orientation);
    	nbttagcompound.setFloat("progress", entity.progress);
    	nbttagcompound.setInteger("energy", entity.energy);
    	nbttagcompound.setInteger("totalBurnTime", totalBurnTime);
    	nbttagcompound.setInteger("burnTime", burnTime);
    	
    	if (itemInInventory != null) {
    		NBTTagCompound cpt = new NBTTagCompound();
    		itemInInventory.writeToNBT(cpt);
    		nbttagcompound.setTag("itemInInventory", cpt);
    	}
    	 
    }

	@Override
	public int minEnergyExpected() {
		return 5;
	}

	@Override
	public int maxEnergyExpected() {
		return entity.maxEnergyReceived();
	}

	@Override
	public void receiveEnergy(int energy) {
		entity.addEnergy((int) (energy * 0.9));		
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return itemInInventory;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack newStack = itemInInventory.splitStack(j);
		
		if (itemInInventory.stackSize == 0) {
			itemInInventory = null;
		}
		
		return newStack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		itemInInventory = itemstack;		
	}

	@Override
	public String getInvName() {
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
	
    private int getItemBurnTime(ItemStack itemstack)
    {
        if(itemstack == null)
        {
            return 0;
        }
        int i = itemstack.getItem().shiftedIndex;
        if(i < 256 && Block.blocksList[i].blockMaterial == Material.wood)
        {
            return 300;
        }
        if(i == Item.stick.shiftedIndex)
        {
            return 100;
        }
        if(i == Item.coal.shiftedIndex)
        {
            return 1600;
        }
        if(i == Item.bucketLava.shiftedIndex)
        {
            return 20000;
        } else
        {
            return i == Block.sapling.blockID ? 100 : ModLoader.AddAllFuel(i);
        }
    }
    
    public boolean isBurning()
    {
        return burnTime > 0;
    }
    
    public int getBurnTimeRemainingScaled(int i) {
        return (burnTime * i) / totalBurnTime;
    }

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		return false;
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		return null;
	}
	
}
