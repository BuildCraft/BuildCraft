/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileEngine extends TileBuildCraft implements IPowerReceptor,
		IInventory, ILiquidContainer, IEngineProvider {
	
	public @TileNetworkData Engine engine;	
	public @TileNetworkData int progressPart = 0;	
	public @TileNetworkData float serverPistonSpeed = 0;
	
	boolean lastPower = false;

	public int orientation;
	
	private ItemStack itemInInventory;	
	
	PowerProvider provider;
	
	public TileEngine () {
		provider = PowerFramework.currentFramework.createPowerProvider();		
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
					
					sendNetworkUpdate();
				}
			}
		} else {
			// If we're not in an active movement process, update the client
			// from time to time in order to e.g. display proper color.
			
			if (worldObj.getWorldTime() % 20 * 10 == 0) {
				sendNetworkUpdate ();
			}
		}
		
		engine.burn ();
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
		engine.delete();
	
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
    	
    	if (nbttagcompound.hasKey("itemInInventory")) {
    		NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
    		itemInInventory = ItemStack.loadItemStackFromNBT(cpt);
    	}
    	
    	engine.readFromNBT(nbttagcompound);
    }
    

    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	
		nbttagcompound.setInteger("kind",
				worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
		nbttagcompound.setInteger("orientation", orientation);
    	nbttagcompound.setFloat("progress", engine.progress);
    	nbttagcompound.setInteger("energy", engine.energy);
    	
    	if (itemInInventory != null) {
    		NBTTagCompound cpt = new NBTTagCompound();
    		itemInInventory.writeToNBT(cpt);
    		nbttagcompound.setTag("itemInInventory", cpt);
    	}
    	 
    	engine.writeToNBT(nbttagcompound);
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}    
    
    public boolean isBurning()
    {
        return engine != null && engine.isBurning();
    }
    
    public int getScaledBurnTime(int i) {
        return engine.getScaledBurnTime(i);
    }
	
   
    @Override
	public Packet getDescriptionPacket () {
		createEngineIfNeeded ();
			
		return super.getDescriptionPacket();
	}
	
	@Override
	public Packet getUpdatePacket () {
		serverPistonSpeed = engine.getPistonSpeed();
		
		return super.getUpdatePacket();
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		createEngineIfNeeded();
		
		super.handleDescriptionPacket(packet);
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		createEngineIfNeeded();
		
		super.handleUpdatePacket(packet);
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
				engine.maxEnergyReceived(), true) * 0.95F));
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

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (engine instanceof EngineIron) {
			return ((EngineIron) engine).fill(from, quantity, id, true);
		} else {		
			return 0;
		}
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		return 0;
	}

	@Override
	public int getLiquidQuantity() {
		return 0;
	}

	@Override
	public int getCapacity() {
		return API.BUCKET_VOLUME * 10;
	}

	@Override
	public int getLiquidId() {
		return 0;
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
	
	@Override
	public int powerRequest() {
		return 0;
	}

	@Override
	public Engine getEngine() {
		return engine;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1){
		return null;
	}
}
