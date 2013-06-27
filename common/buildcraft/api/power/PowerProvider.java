/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.SafeTimeTracker;

public final class PowerProvider {

	public static class PerditionCalculator {

		public static final float DEFAULT_POWERLOSS = 10F;
		public static final float MIN_POWERLOSS = 0.01F;
		protected final SafeTimeTracker energyLossTracker = new SafeTimeTracker();
		private final float powerLoss;

		public PerditionCalculator() {
			powerLoss = DEFAULT_POWERLOSS;
		}

		public PerditionCalculator(float powerLoss) {
			if (powerLoss < MIN_POWERLOSS) {
				powerLoss = MIN_POWERLOSS;
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
	public static final PerditionCalculator DEFUALT_PERDITION = new PerditionCalculator();
	private int minEnergyReceived;
	private int maxEnergyReceived;
	private int maxEnergyStored;
	private int activationEnergy;
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
		return this.activationEnergy;
	}

	public float getEnergyStored() {
		return this.energyStored;
	}

	/**
	 * Setup your PowerProvider's settings.
	 *
	 * @param minEnergyReceived This is the minimum about of power that will be
	 * accepted by the PowerProvider. This should generally be greater than the
	 * activationEnergy if you plan to use the doWork() callback. Anything
	 * greater than 1 will prevent Redstone Engines from powering this Provider.
	 * @param maxEnergyReceived The maximum amount of power accepted by the
	 * PowerProvider. This should generally be less than 500. Too low and larger
	 * engines will overheat while trying to power the machine. Too high, and
	 * the engines will never warm up. Greater values also place greater strain
	 * on the power net.
	 * @param activationEnergy If the stored energy is greater than this value,
	 * the doWork() callback is called (once per tick).
	 * @param maxStoredEnergy The maximum amount of power this PowerProvider can
	 * store. Values tend to range between 100 and 5000. With 1000 and 1500
	 * being common.
	 */
	public void configure(int minEnergyReceived, int maxEnergyReceived, int activationEnergy, int maxStoredEnergy) {
		if (minEnergyReceived > maxEnergyReceived) {
			maxEnergyReceived = minEnergyReceived;
		}
		this.minEnergyReceived = minEnergyReceived;
		this.maxEnergyReceived = maxEnergyReceived;
		this.maxEnergyStored = maxStoredEnergy;
		this.activationEnergy = activationEnergy;
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
		if (powerLoss == 0 || powerLossRegularity == 0) {
			perdition = new PerditionCalculator(0);
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
			if (energyLossTracker.markTimeIfDelay(receptor.getWorldObj(), 10)) {
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
		if (energyStored >= activationEnergy) {
			if (doWorkTracker.markTimeIfDelay(receptor.getWorldObj(), 1)) {
				receptor.doWork(this);
			}
		}
	}

	/**
	 * Extract energy from the PowerProvider. You must call this even if
	 * doWork() triggers.
	 *
	 * @param min
	 * @param max
	 * @param doUse
	 * @return amount used
	 */
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

	/**
	 * The amount of power that this PowerProvider currently needs.
	 *
	 * @return
	 */
	public float powerRequest() {
		return Math.min(maxEnergyReceived, maxEnergyStored - energyStored);
	}

	/**
	 * Add power to the Provider from an external source.
	 *
	 * @param quantity
	 * @param from
	 * @return the amount of power used
	 */
	public float receiveEnergy(float quantity, ForgeDirection from) {
		if (quantity > maxEnergyReceived) {
			quantity -= quantity - maxEnergyReceived;
		}
		if (from != null)
			powerSources[from.ordinal()] = 2;

		quantity = addEnergy(quantity);
		applyWork();

		return quantity;
	}

	/**
	 * Internal use only you should NEVER call this function on a PowerProvider
	 * you don't own.
	 *
	 * @return the amount the power changed by
	 */
	public float addEnergy(float quantity) {
		energyStored += quantity;

		if (energyStored > maxEnergyStored) {
			quantity -= energyStored - maxEnergyStored;
			energyStored = maxEnergyStored;
		} else if (energyStored < 0) {
			quantity -= energyStored;
			energyStored = 0;
		}

		applyPerdition();

		return quantity;
	}

	/**
	 * Internal use only you should NEVER call this function on a PowerProvider
	 * you don't own.
	 */
	public void setEnergy(float quantity) {
		this.energyStored = quantity;
		if (energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		} else if (energyStored < 0) {
			energyStored = 0;
		}
	}

	public boolean isPowerSource(ForgeDirection from) {
		return powerSources[from.ordinal()] != 0;
	}
}
