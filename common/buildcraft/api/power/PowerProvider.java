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

	public static class PerditionCalculator {

		protected final SafeTimeTracker energyLossTracker = new SafeTimeTracker();
		private final float powerLoss;

		public PerditionCalculator() {
			powerLoss = 10;
		}

		public PerditionCalculator(float powerLoss) {
			if (powerLoss < 0) {
				powerLoss = 10;
			}
			this.powerLoss = powerLoss;
		}

		public float applyPerdition(PowerProvider provider, float current) {
			current -= powerLoss;
			if (current < 0) {
				current = 0;
			}
			return current;
		}
	}
	public static final PerditionCalculator DEFUALT_PERDITION = new PerditionCalculator(10);
	private int minEnergyReceived;
	private int maxEnergyReceived;
	private int maxEnergyStored;
	private int minActivationEnergy;
	private float energyStored = 0;
	public final boolean canAcceptPowerFromPipes;
	private final SafeTimeTracker doWorkTracker = new SafeTimeTracker();
	private final SafeTimeTracker energyLossTracker = new SafeTimeTracker();
	public final int[] powerSources = {0, 0, 0, 0, 0, 0};
	public final IPowerReceptor receptor;
	private PerditionCalculator perdition;

	public PowerProvider(IPowerReceptor receptor) {
		this(receptor, true);
	}

	public PowerProvider(IPowerReceptor receptor, boolean canAcceptPowerFromPipes) {
		this.canAcceptPowerFromPipes = canAcceptPowerFromPipes;
		this.receptor = receptor;
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

	public void update() {
		applyPerdition();
		applyWork();

		for (int i = 0; i < 6; ++i) {
			if (powerSources[i] > 0) {
				powerSources[i]--;
			}
		}
	}

	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		if (powerLossRegularity == 0) {
			return;
		}
		perdition = new PerditionCalculator((float) powerLoss / (float) powerLossRegularity * 10.0F);
	}

	public void setPerdition(PerditionCalculator perdition) {
		this.perdition = perdition;
	}

	public PerditionCalculator getPerdition() {
		if (perdition == null)
			return DEFUALT_PERDITION;
		return perdition;
	}

	private void applyPerdition() {
		if (energyStored > 0) {
			TileEntity tile = (TileEntity) receptor;
			if (energyLossTracker.markTimeIfDelay(tile.worldObj, 10)) {
				float newEnergy = getPerdition().applyPerdition(this, energyStored);
				if (newEnergy == 0 || newEnergy < energyStored) {
					energyStored = newEnergy;
				} else {
					energyStored = DEFUALT_PERDITION.applyPerdition(this, energyStored);
				}
			}
		}
	}

	private void applyWork() {
		if (energyStored >= minActivationEnergy) {
			TileEntity tile = (TileEntity) receptor;
			if (doWorkTracker.markTimeIfDelay(tile.worldObj, 1)) {
				receptor.doWork(this);
			}
		}
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
		if (from != null)
			powerSources[from.ordinal()] = 2;

		quantity = addEnergy(quantity);
		applyWork();

		return quantity;
	}

	/**
	 * Internal use only you should NEVER call this function on a PowerProvider
	 * you don't own.
	 */
	public float addEnergy(float quantity) {
		energyStored += quantity;

		if (energyStored > maxEnergyStored) {
			quantity -= energyStored - maxEnergyStored;
			energyStored = maxEnergyStored;
		} else if (energyStored < 0) {
			energyStored = 0;
		}

		applyPerdition();

		return quantity;
	}

	/**
	 * Internal use only you should NEVER call this function on a PowerProvider
	 * you don't own.
	 */
	public void setEnergy(float energy) {
		this.energyStored = energy;
	}

	public boolean isPowerSource(ForgeDirection from) {
		return powerSources[from.ordinal()] != 0;
	}
}
