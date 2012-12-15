/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftCore;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;

public class RedstonePowerProvider extends PowerProvider {

	private boolean lastPower = false;

	public RedstonePowerProvider() {
		this.powerLoss = 0;
		this.powerLossRegularity = 0;
	}

	@Override
	public boolean preConditions(IPowerReceptor receptor) {
		TileEntity tile = (TileEntity) receptor;

		boolean currentPower = tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);

		if (BuildCraftCore.continuousCurrentModel) {
			if (currentPower)
				return true;
		} else if (currentPower != lastPower) {
			lastPower = currentPower;

			if (currentPower)
				return true;
		}

		return false;
	}

	@Override
	public float useEnergy(float min, float max, boolean doUse) {
		return min;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		lastPower = nbttagcompound.getBoolean("lastPower");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		nbttagcompound.setBoolean("lastPower", lastPower);
	}

	@Override
	public void configure(int latency, int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived, minActivationEnergy, maxStoredEnergy);

		this.minActivationEnergy = 0;
		this.energyStored = 1;
	}

	@Override
	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {

	}
}
