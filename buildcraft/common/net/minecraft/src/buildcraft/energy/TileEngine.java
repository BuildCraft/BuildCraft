package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftEnergy;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.IPowerReceptor;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;

public class TileEngine extends TileEntity implements IPowerReceptor,
		IInventory, ISynchronizedTile {

	boolean init = false;
	
	Engine entity;
	
	boolean lastPower = false;
	
	int progressPart = 0;

	public int orientation;
	
	private ItemStack itemInInventory;
	public int burnTime = 0;
	public int totalBurnTime = 0;
	
	@Override
	public void updateEntity () {
		if (!APIProxy.isClient(worldObj)) {
			if (!init) {
				if (entity == null) {
					createEngineIfNeeded ();
				}

				entity.orientation = Orientations.values()[orientation];			

				init = true;
			}
		} else {
			if (entity == null) {
				return;
			}
		}
		
		entity.update();
		
		boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
		
		if (progressPart != 0) {
			entity.progress += entity.getPistonSpeed();
			
			if (entity.progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				
				Position pos = new Position(xCoord, yCoord, zCoord,
						entity.orientation);
				pos.moveForwards(1.0);
				TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
						(int) pos.z);
				
				if (isPoweredTile(tile)) {
					IPowerReceptor receptor = (IPowerReceptor) tile;
					
					int minEnergy = receptor.getPowerProvider().minEnergyReceived;
					
					if (minEnergy != -1 && minEnergy <= entity.energy) {
						int energySent = receptor.getPowerProvider().maxEnergyReceived;
						
						if (entity.energy < energySent) {
							energySent = entity.energy;
						}
						
						entity.energy -= energySent;
						receptor.getPowerProvider().receiveEnergy(energySent);
					}
				}
			} else if (entity.progress >= 1) {
				entity.progress = 0;
				progressPart = 0;
			}
		} else if (isPowered) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					entity.orientation);
			pos.moveForwards(1.0);
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);

			if (isPoweredTile(tile)) {
				IPowerReceptor receptor = (IPowerReceptor) tile;
				int minEnergy = receptor.getPowerProvider().minEnergyReceived;

				if (minEnergy != -1 && minEnergy <= entity.energy) {
					progressPart = 1;										
				}
			}
		}

		if (entity instanceof EngineStone) {
			if(burnTime > 0) {
				burnTime--;
				entity.addEnergy(1);
			}

			if (burnTime == 0 && isPowered) {
				burnTime = totalBurnTime = getItemBurnTime(itemInInventory);
				if (burnTime > 0) {
					decrStackSize(1, 1);				
				}
			}
		} else if (entity instanceof EngineIron) {
			if (isPowered) {
				if(burnTime > 0) {
					burnTime--;
					entity.addEnergy(2);					
				}
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
	
	private void createEngineIfNeeded() {
		if (entity == null) {
			int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (kind == 0) {
				entity = new EngineWood(this);
			} else if (kind == 1) {
				entity = new EngineStone(this);
			} else if (kind == 2) {
				entity = new EngineIron(this);
			}

			entity.orientation = Orientations.values()[orientation];
		}
	}

	public void switchOrientation () {						
		for (int i = orientation + 1; i <= orientation + 6; ++i) {
			Orientations o = Orientations.values() [i % 6];
			
			Position pos = new Position (xCoord, yCoord, zCoord, o);
			
			pos.moveForwards(1);
			
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);
			
			if (isPoweredTile(tile)) {				
				if (entity != null) {
					entity.orientation = o;	
				}				
				orientation = o.ordinal();
				break;			
			}
		}
	}
	
	public void delete () {
	
	}
	
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
    	super.readFromNBT(nbttagcompound);
    	
		int kind = nbttagcompound.getInteger("kind");
		
		if (kind == 0) {
			entity = new EngineWood(this);
		} else if (kind == 1) {
			entity = new EngineStone(this);
		} else if (kind == 2) {
			entity = new EngineIron(this);
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
        return entity != null && entity.isBurning();
    }
    
    public int getBurnTimeRemainingScaled(int i) {
        return (burnTime * i) / totalBurnTime;
    }
	
	public Packet getDescriptionPacket () {
		createEngineIfNeeded ();
		
		Packet230ModLoader packet = new Packet230ModLoader();
		
		packet.modId = mod_BuildCraftEnergy.instance.getId();
		packet.packetType = PacketIds.EngineDescription.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [5];
		
		packet.dataFloat = new float [1];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		packet.dataInt [3] = entity.orientation.ordinal();
		packet.dataInt [4] = entity.energy;
		
		packet.dataFloat [0] = entity.progress;
		
		
		
		return packet;
	}
	
	public Packet230ModLoader getUpdatePacket () {
		Packet230ModLoader packet = new Packet230ModLoader();
		
		packet.modId = mod_BuildCraftEnergy.instance.getId();
		packet.packetType = PacketIds.EngineUpdate.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [5];
		
		packet.dataFloat = new float [1];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;		
		packet.dataInt [3] = entity.orientation.ordinal();
		packet.dataInt [4] = entity.energy;		
		
		packet.dataFloat [0] = entity.progress;
						
		return packet;
	}

	@Override
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		if (entity == null) {
			int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			
			if (kind == 0) {
				entity = new EngineWood(this);
			} else if (kind == 1) {
				entity = new EngineStone(this);
			} else if (kind == 2) {
				entity = new EngineIron(this);
			}
		}
		
		entity.orientation = Orientations.values() [packet.dataInt[3]];
		entity.progress = packet.dataFloat [0];		
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		entity.orientation = Orientations.values() [packet.dataInt[3]];
		entity.progress = packet.dataFloat [0];		
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PowerProvider getPowerProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isPoweredTile (TileEntity tile) {
		if (tile instanceof IPowerReceptor) {
			IPowerReceptor receptor = (IPowerReceptor) tile;
			PowerProvider provider = receptor.getPowerProvider();
			
			return provider != null
					&& provider.getClass().equals(PneumaticPowerProvider.class);				
		}
		
		return false;
	}
	
}
