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
	
	@Override
	public void configure(int latency, int minEnergyReceived,
			int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived,
				minActivationEnergy, maxStoredEnergy);

		this.latency = 0;
	}

	@Override
	public void update(IPowerReceptor receptor) {
		workIfCondition(receptor);	
	}

}
