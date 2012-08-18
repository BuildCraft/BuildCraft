/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.core.ProxyCore;
import buildcraft.core.network.TileNetworkData;
import buildcraft.energy.gui.ContainerEngine;
import net.minecraft.src.ICrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public abstract class Engine {

	public int maxEnergy;

	protected float currentOutput = 0;
	public @TileNetworkData
	float progress;
	public @TileNetworkData
	Orientations orientation;
	public float energy;
	public @TileNetworkData
	EnergyStage energyStage = EnergyStage.Blue;

	public int maxEnergyExtracted = 1;

	protected TileEngine tile;

	public enum EnergyStage {
		Blue, Green, Yellow, Red, Explosion
	}

	public Engine(TileEngine tile) {
		this.tile = tile;
	}

	protected void computeEnergyStage() {
		if (energy / (double) maxEnergy * 100.0 <= 25.0) {
			energyStage = EnergyStage.Blue;
		} else if (energy / (double) maxEnergy * 100.0 <= 50.0) {
			energyStage = EnergyStage.Green;
		} else if (energy / (double) maxEnergy * 100.0 <= 75.0) {
			energyStage = EnergyStage.Yellow;
		} else if (energy / (double) maxEnergy * 100.0 <= 100.0) {
			energyStage = EnergyStage.Red;
		} else {
			energyStage = EnergyStage.Explosion;
		}
	}

	public final EnergyStage getEnergyStage() {
		if (!ProxyCore.proxy.isRemote(tile.worldObj)) {
			computeEnergyStage();
		}

		return energyStage;
	}

	public void update() {
		if (!tile.isRedstonePowered) {
			if (energy > 1) {
				energy -= 1;
			}
		}
	}

	public abstract String getTextureFile();

	public abstract int explosionRange();

	public abstract int maxEnergyReceived();

	public abstract float getPistonSpeed();

	public abstract boolean isBurning();

	public abstract void delete();

	public void addEnergy(float addition) {
		energy += addition;

		if (getEnergyStage() == EnergyStage.Explosion) {
			tile.worldObj.createExplosion(null, tile.xCoord, tile.yCoord, tile.zCoord, explosionRange());
		}

		if (energy > maxEnergy) {
			energy = maxEnergy;
		}
	}

	public float extractEnergy(int min, int max, boolean doExtract) {
		if (energy < min) {
			return 0;
		}

		int actualMax;

		if (max > maxEnergyExtracted) {
			actualMax = maxEnergyExtracted;
		} else {
			actualMax = max;
		}

		float extracted;

		if (energy >= actualMax) {
			extracted = actualMax;
			if (doExtract) {
				energy -= actualMax;
			}
		} else {
			extracted = energy;
			if (doExtract) {
				energy = 0;
			}
		}

		return extracted;
	}

	public abstract int getScaledBurnTime(int i);

	public abstract void burn();

	public void readFromNBT(NBTTagCompound nbttagcompound) {

	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {

	}

	public void getGUINetworkData(int i, int j) {

	}

	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {

	}

	public boolean isActive() {
		return true;
	}

	public int getHeat() {
		return 0;
	}

	public float getEnergyStored() {
		return energy;
	}

	public float getCurrentOutput() {
		return currentOutput;
	}

	/* ILIQUIDCONTAINER */
	public LiquidTank[] getLiquidSlots() {
		return new LiquidTank[0];
	}

	/* IINVENTORY */
	public int getSizeInventory() { return 0; }
	public ItemStack getStackInSlot(int i) { return null; }
	public ItemStack decrStackSize(int i, int j) { return null; }
	public ItemStack getStackInSlotOnClosing(int i) { return getStackInSlot(i); }
	public void setInventorySlotContents(int i, ItemStack itemstack) {}

}
