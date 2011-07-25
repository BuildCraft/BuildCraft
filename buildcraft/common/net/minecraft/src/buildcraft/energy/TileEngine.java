package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
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
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.IPowerReceptor;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public class TileEngine extends TileBuildCraft implements IPowerReceptor,
		IInventory, ISynchronizedTile {

	boolean init = false;
	
	public Engine engine;
	
	boolean lastPower = false;
	
	int progressPart = 0;

	public int orientation;
	
	private ItemStack itemInInventory;
	public int burnTime = 0;
	public int totalBurnTime = 0;
	PowerProvider provider;

	public float serverPistonSpeed = 0;
	
	public TileEngine () {
		provider = BuildCraftCore.powerFramework.createPowerProvider();		
	}
	
	@Override
	public void initialize() {
		if (!APIProxy.isClient(worldObj)) {
			if (engine == null) {
				createEngineIfNeeded();
			}

			engine.orientation = Orientations.values()[orientation];
			provider.configure(0, 1, engine.maxEnergyReceived(), 1,
					engine.maxEnergy);
		}
	}
	
	@Override
	public void updateEntity () {
		super.updateEntity();
		
		if (engine == null) {
			return;
		}
		
		if (APIProxy.isClient(worldObj)) {
			if (progressPart != 0) {
				engine.progress += serverPistonSpeed;
				
				if (engine.progress > 1) {
					progressPart = 0;
				}
			}
			
			return;
		}
		
		engine.update();
		
		boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
		
		if (progressPart != 0) {
			engine.progress += engine.getPistonSpeed();
			
			if (engine.progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				
				Position pos = new Position(xCoord, yCoord, zCoord,
						engine.orientation);
				pos.moveForwards(1.0);
				TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
						(int) pos.z);
				
				if (isPoweredTile(tile)) {
					IPowerReceptor receptor = (IPowerReceptor) tile;
					
					int extracted = engine.extractEnergy(
							receptor.getPowerProvider().minEnergyReceived,
							receptor.getPowerProvider().maxEnergyReceived, true);
					
					if (extracted > 0) {
						receptor.getPowerProvider().receiveEnergy(extracted);
					}
				}
			} else if (engine.progress >= 1) {
				engine.progress = 0;
				progressPart = 0;
			}
		} else if (isPowered) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					engine.orientation);
			pos.moveForwards(1.0);
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);

			if (isPoweredTile(tile)) {
				IPowerReceptor receptor = (IPowerReceptor) tile;

				if (engine.extractEnergy(
						receptor.getPowerProvider().minEnergyReceived,
						receptor.getPowerProvider().maxEnergyReceived, false) > 0) {
					progressPart = 1;
					
					CoreProxy.sendToPlayers(getUpdatePacket(), xCoord, yCoord, zCoord,
							50, mod_BuildCraftEnergy.instance);
				}
			}
		} else {
			// If we're not in an active movement process, update the client
			// from time to time in order to e.g. display proper color.
			
			if (worldObj.getWorldTime() % 20 * 10 == 0) {
				CoreProxy.sendToPlayers(getUpdatePacket(), xCoord, yCoord, zCoord,
						50, mod_BuildCraftEnergy.instance);
			}
		}

		if (engine instanceof EngineStone) {
			if(burnTime > 0) {
				burnTime--;
				engine.addEnergy(1);
			}

			if (burnTime == 0 && isPowered) {
				burnTime = totalBurnTime = getItemBurnTime(itemInInventory);
				if (burnTime > 0) {
					decrStackSize(1, 1);				
				}
			}
		} else if (engine instanceof EngineIron) {
			if (isPowered) {
				if(burnTime > 0) {
					burnTime--;
					engine.addEnergy(2);					
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
		if (engine == null) {
			int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (kind == 0) {
				engine = new EngineWood(this);
			} else if (kind == 1) {
				engine = new EngineStone(this);
			} else if (kind == 2) {
				engine = new EngineIron(this);
			}

			engine.orientation = Orientations.values()[orientation];
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
				if (engine != null) {
					engine.orientation = o;	
				}				
				orientation = o.ordinal();
				worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
				
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
			engine = new EngineWood(this);
		} else if (kind == 1) {
			engine = new EngineStone(this);
		} else if (kind == 2) {
			engine = new EngineIron(this);
		}
		
		orientation = nbttagcompound.getInteger("orientation");
    	engine.progress = nbttagcompound.getFloat("progress");
    	engine.energy = nbttagcompound.getInteger("energy");
    	engine.orientation = Orientations.values()[orientation];
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
    	nbttagcompound.setFloat("progress", engine.progress);
    	nbttagcompound.setInteger("energy", engine.energy);
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
		return "Engine";
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
        return engine != null && engine.isBurning();
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
		
		packet.dataInt = new int [6];
		
		packet.dataFloat = new float [1];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		packet.dataInt [3] = engine.orientation.ordinal();
		packet.dataInt [4] = engine.energy;
		packet.dataInt [5] = burnTime;
		
		packet.dataFloat [0] = engine.progress;		
		
		return packet;
	}
	
	public Packet230ModLoader getUpdatePacket () {
		Packet230ModLoader packet = new Packet230ModLoader();
		
		packet.modId = mod_BuildCraftEnergy.instance.getId();
		packet.packetType = PacketIds.EngineUpdate.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [7];		
		packet.dataFloat = new float [2];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;		
		packet.dataInt [3] = engine.orientation.ordinal();
		packet.dataInt [4] = engine.energy;	
		packet.dataInt [5] = progressPart;
		packet.dataInt [6] = burnTime;
		
		packet.dataFloat [0] = engine.progress;
		packet.dataFloat [1] = engine.getPistonSpeed();
		
		return packet;
	}

	@Override
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		if (engine == null) {
			int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			
			if (kind == 0) {
				engine = new EngineWood(this);
			} else if (kind == 1) {
				engine = new EngineStone(this);
			} else if (kind == 2) {
				engine = new EngineIron(this);
			}
		}
		
		engine.orientation = Orientations.values() [packet.dataInt[3]];
		engine.progress = packet.dataFloat [0];			
		engine.energy = packet.dataInt [4];
		burnTime = packet.dataInt [5];
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		engine.orientation = Orientations.values() [packet.dataInt[3]];
		engine.progress = packet.dataFloat [0];	
		engine.energy = packet.dataInt [4];
		serverPistonSpeed = packet.dataFloat [1];
		progressPart = packet.dataInt [5];
		burnTime = packet.dataInt [6];
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		this.provider = provider;		
	}

	@Override
	public PowerProvider getPowerProvider() {
		return provider;
	}

	@Override
	public void doWork() {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		engine.addEnergy((int) (provider.useEnergy(1,
				engine.maxEnergyReceived(), true) * 0.9F));
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
