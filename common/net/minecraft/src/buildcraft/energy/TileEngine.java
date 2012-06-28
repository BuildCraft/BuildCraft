/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IOverrideDefaultTriggers;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.core.IBuilderInventory;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

//TODO: All Engines need to take func_48081_b into account 

public class TileEngine extends TileBuildCraft implements IPowerReceptor, IInventory, ILiquidContainer, IEngineProvider,
		IOverrideDefaultTriggers, IPipeConnection, IBuilderInventory {

	public @TileNetworkData
	Engine engine;
	public @TileNetworkData
	int progressPart = 0;
	public @TileNetworkData
	float serverPistonSpeed = 0;
	public @TileNetworkData
	boolean isActive = false; // Used for SMP synch

	boolean lastPower = false;

	public int orientation;

	private ItemStack itemInInventory;

	PowerProvider provider;

	public boolean isRedstonePowered = false;

	public TileEngine() {
		provider = PowerFramework.currentFramework.createPowerProvider();
	}

	@Override
	public void initialize() {
		if (!APIProxy.isClient(worldObj)) {
			if (engine == null) {
				createEngineIfNeeded();
			}

			engine.orientation = Orientations.values()[orientation];
			provider.configure(0, 1, engine.maxEnergyReceived(), 1, engine.maxEnergy);
			checkRedstonePower();
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (engine == null) {
			return;
		}

		if (APIProxy.isClient(worldObj)) {
			if (progressPart != 0) {
				engine.progress += serverPistonSpeed;

				if (engine.progress > 1) {
					progressPart = 0;
					engine.progress = 0;
				}
			} else if(this.isActive)
				progressPart = 1;

			return;
		}

		engine.update();

		float newPistonSpeed = engine.getPistonSpeed();
		if (newPistonSpeed != serverPistonSpeed) {
			serverPistonSpeed = newPistonSpeed;
			sendNetworkUpdate();
		}

		if (progressPart != 0) {
			engine.progress += engine.getPistonSpeed();

			if (engine.progress > 0.5 && progressPart == 1) {
				progressPart = 2;

				Position pos = new Position(xCoord, yCoord, zCoord, engine.orientation);
				pos.moveForwards(1.0);
				TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

				if (isPoweredTile(tile)) {
					IPowerReceptor receptor = (IPowerReceptor) tile;

					int extracted = engine.extractEnergy(receptor.getPowerProvider().minEnergyReceived,
							receptor.getPowerProvider().maxEnergyReceived, true);

					if (extracted > 0) {
						receptor.getPowerProvider().receiveEnergy(extracted, engine.orientation.reverse());
					}
				}
			} else if (engine.progress >= 1) {
				engine.progress = 0;
				progressPart = 0;
			}
		} else if (isRedstonePowered && engine.isActive()) {
			
			Position pos = new Position(xCoord, yCoord, zCoord, engine.orientation);
			pos.moveForwards(1.0);
			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

			if (isPoweredTile(tile)) {
				IPowerReceptor receptor = (IPowerReceptor) tile;

				if (engine.extractEnergy(receptor.getPowerProvider().minEnergyReceived,
						receptor.getPowerProvider().maxEnergyReceived, false) > 0) {
					progressPart = 1;
					setActive(true);
				} else
					setActive(false);
			} else
				setActive(false);
			
		} else
			setActive(false);

		engine.burn();
	}

	private void setActive(boolean isActive) {
		if(this.isActive == isActive)
			return;
		
		this.isActive = isActive;
		sendNetworkUpdate();
	}
	
	private void createEngineIfNeeded() {
		if (engine == null) {
			int kind = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			engine = newEngine(kind);

			engine.orientation = Orientations.values()[orientation];
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, BuildCraftEnergy.engineBlock.blockID);
		}
	}

	public void switchOrientation() {
		for (int i = orientation + 1; i <= orientation + 6; ++i) {
			Orientations o = Orientations.values()[i % 6];

			Position pos = new Position(xCoord, yCoord, zCoord, o);

			pos.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

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

	public void delete() {
		if (engine != null) {
			engine.delete();
		}
	}

	public Engine newEngine(int meta) {
		if (meta == 0) {
			return new EngineWood(this);
		} else if (meta == 1) {
			return new EngineStone(this);
		} else if (meta == 2) {
			return new EngineIron(this);
		} else {
			return null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		int kind = nbttagcompound.getInteger("kind");

		engine = newEngine(kind);

		orientation = nbttagcompound.getInteger("orientation");

		if (engine != null) {
			engine.progress = nbttagcompound.getFloat("progress");
			engine.energy = nbttagcompound.getInteger("energy");
			engine.orientation = Orientations.values()[orientation];
		}

		if (nbttagcompound.hasKey("itemInInventory")) {
			NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
			itemInInventory = ItemStack.loadItemStackFromNBT(cpt);
		}

		if (engine != null) {
			engine.readFromNBT(nbttagcompound);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("kind", worldObj.getBlockMetadata(xCoord, yCoord, zCoord));

		if (engine != null) {
			nbttagcompound.setInteger("orientation", orientation);
			nbttagcompound.setFloat("progress", engine.progress);
			nbttagcompound.setInteger("energy", engine.energy);
		}

		if (itemInInventory != null) {
			NBTTagCompound cpt = new NBTTagCompound();
			itemInInventory.writeToNBT(cpt);
			nbttagcompound.setTag("itemInInventory", cpt);
		}

		if (engine != null) {
			engine.writeToNBT(nbttagcompound);
		}
	}

	@Override
	public int getSizeInventory() {
		if (engine instanceof EngineStone) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return itemInInventory;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (itemInInventory != null) {
			ItemStack newStack = itemInInventory.splitStack(j);

			if (itemInInventory.stackSize == 0) {
				itemInInventory = null;
			}

			return newStack;
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (itemInInventory == null)
			return null;
		ItemStack toReturn = itemInInventory;
		itemInInventory = null;
		return toReturn;
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

	public boolean isBurning() {
		return engine != null && engine.isBurning();
	}

	public int getScaledBurnTime(int i) {
		if (engine != null) {
			return engine.getScaledBurnTime(i);
		} else {
			return 0;
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		createEngineIfNeeded();

		return super.getDescriptionPacket();
	}

	@Override
	public Packet getUpdatePacket() {
		if (engine != null) {
			serverPistonSpeed = engine.getPistonSpeed();
		}

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

		engine.addEnergy((int) (provider.useEnergy(1, engine.maxEnergyReceived(), true) * 0.95F));
	}

	public boolean isPoweredTile(TileEntity tile) {
		if (tile instanceof IPowerReceptor) {
			IPowerReceptor receptor = (IPowerReceptor) tile;
			PowerProvider provider = receptor.getPowerProvider();

			return provider != null && provider.getClass().equals(PneumaticPowerProvider.class);
		}

		return false;
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (engine instanceof EngineIron) {
			return ((EngineIron) engine).fill(from, quantity, id, doFill);
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
	public LinkedList<Trigger> getTriggers() {
		LinkedList<Trigger> triggers = new LinkedList<Trigger>();

		triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
		triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
		triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
		triggers.add(BuildCraftEnergy.triggerRedEngineHeat);

		if (engine instanceof EngineIron) {
			triggers.add(BuildCraftCore.triggerEmptyLiquid);
			triggers.add(BuildCraftCore.triggerContainsLiquid);
			triggers.add(BuildCraftCore.triggerSpaceLiquid);
			triggers.add(BuildCraftCore.triggerFullLiquid);
		} else if (engine instanceof EngineStone) {
			triggers.add(BuildCraftCore.triggerEmptyInventory);
			triggers.add(BuildCraftCore.triggerContainsInventory);
			triggers.add(BuildCraftCore.triggerSpaceInventory);
			triggers.add(BuildCraftCore.triggerFullInventory);
		}

		return triggers;
	}

	@Override
	public LiquidSlot[] getLiquidSlots() {
		if (engine == null) {
			return new LiquidSlot[0];
		} else {
			return engine.getLiquidSlots();
		}
	}

	@Override
	public boolean isPipeConnected(Orientations with) {
		if (engine instanceof EngineWood) {
			return false;
		}

		return with.ordinal() != orientation;
	}

	@Override
	public boolean isBuildingMaterial(int i) {
		return false;
	}

	public void checkRedstonePower() {
		isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}
}
