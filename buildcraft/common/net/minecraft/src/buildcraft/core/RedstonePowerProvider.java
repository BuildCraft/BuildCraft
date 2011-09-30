/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.PowerProvider;

public class RedstonePowerProvider extends PowerProvider {
	
	private boolean lastPower = false;
	
	public RedstonePowerProvider () {
		this.powerLoss = 0;
		this.powerLossRegularity = 0;
	}
	
	@Override
	public boolean preConditions(IPowerReceptor receptor) {
		TileEntity tile = (TileEntity) receptor;
		
		boolean currentPower = tile.worldObj.isBlockIndirectlyGettingPowered(
				tile.xCoord, tile.yCoord, tile.zCoord);
		
		if (BuildCraftCore.continuousCurrentModel) {
			if (currentPower) {
				return true;
			}
		} else {			
			if (currentPower != lastPower) {
				lastPower = currentPower;

				if (currentPower) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public int useEnergy (int min, int max, boolean doUse) {		
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

	public void configure(int latency, int minEnergyReceived,
			int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived,
				minActivationEnergy, maxStoredEnergy);

		this.minActivationEnergy = 0;
		this.energyStored = 1;
	}
	
	@Override
	public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
		
	}
}
