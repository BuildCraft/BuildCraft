package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.IPowerReceptor;

public class PneumaticPowerProvider extends PowerProvider {

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
	}
	
	public void configure(int latency, int minEnergyReceived,
			int maxEnergyReceived, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived,
				maxStoredEnergy);

		latency = 0;
	}

	@Override
	public void update(IPowerReceptor receptor) {
		// We don't delay here...
		receptor.doWork();		
	}

}
