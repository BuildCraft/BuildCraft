/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.SafeTimeTracker;

public final class PowerProvider {

	protected int minEnergyReceived;
	protected int maxEnergyReceived;
	protected int maxEnergyStored;
	protected int minActivationEnergy;
	protected float energyStored = 0;
	protected int powerLoss = 1;
	protected int powerLossRegularity = 1;
	public final boolean canAcceptPowerFromPipes;
	public SafeTimeTracker energyLossTracker = new SafeTimeTracker();
	public int[] powerSources = {0, 0, 0, 0, 0, 0};

	public PowerProvider() {
		this.canAcceptPowerFromPipes = true;
	}

	public PowerProvider(boolean canAcceptPowerFromPipes) {
		this.canAcceptPowerFromPipes = canAcceptPowerFromPipes;
	}

	public int getMinEnergyReceived() {
		return this.minEnergyReceived;
	}

	public int getMaxEnergyReceived() {
		return this.maxEnergyReceived;
	}

	public int getMaxEnergyStored() {
		return this.maxEnergyStored;
	}

	public int getActivationEnergy() {
		return this.minActivationEnergy;
	}

	public float getEnergyStored() {
		return this.energyStored;
	}

	public void configure(int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		this.minEnergyReceived = minEnergyReceived;
		this.maxEnergyReceived = maxEnergyReceived;
		this.maxEnergyStored = maxStoredEnergy;
		this.minActivationEnergy = minActivationEnergy;
	}

	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		this.powerLoss = powerLoss;
		this.powerLossRegularity = powerLossRegularity;
	}

	public boolean update(IPowerReceptor receptor) {
		TileEntity tile = (TileEntity) receptor;
		boolean result = false;

		if (energyStored >= minActivationEnergy) {
			receptor.doWork(this);
			result = true;
		}

		if (powerLoss > 0 && energyLossTracker.markTimeIfDelay(tile.worldObj, powerLossRegularity)) {

			energyStored -= powerLoss;
			if (energyStored < 0) {
				energyStored = 0;
			}
		}

		for (int i = 0; i < 6; ++i) {
			if (powerSources[i] > 0) {
				powerSources[i]--;
			}
		}

		return result;
	}

	public float useEnergy(float min, float max, boolean doUse) {
		float result = 0;

		if (energyStored >= min) {
			if (energyStored <= max) {
				result = energyStored;
				if (doUse) {
					energyStored = 0;
				}
			} else {
				result = max;
				if (doUse) {
					energyStored -= max;
				}
			}
		}

		return result;
	}

	public void readFromNBT(NBTTagCompound data) {
		readFromNBT(data, "powerProvider");
	}

	public void readFromNBT(NBTTagCompound data, String tag) {
		NBTTagCompound nbt = data.getCompoundTag(tag);
		energyStored = nbt.getFloat("storedEnergy");
	}

	public void writeToNBT(NBTTagCompound data) {
		writeToNBT(data, "powerProvider");
	}

	public void writeToNBT(NBTTagCompound data, String tag) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("storedEnergy", energyStored);
		data.setCompoundTag(tag, nbt);
	}

	public float powerRequest() {
		return Math.min(maxEnergyReceived, maxEnergyStored - energyStored);
	}

	public float receiveEnergy(float quantity, ForgeDirection from) {
		powerSources[from.ordinal()] = 2;

		energyStored += quantity;

		if (energyStored > maxEnergyStored) {
			quantity -= energyStored - maxEnergyStored;
			energyStored = maxEnergyStored;
		}
		return quantity;
	}

	public boolean isPowerSource(ForgeDirection from) {
		return powerSources[from.ordinal()] != 0;
	}
}
