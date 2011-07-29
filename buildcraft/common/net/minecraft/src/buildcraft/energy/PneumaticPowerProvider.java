package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.buildcraft.api.PowerProvider;

public class PneumaticPowerProvider extends PowerProvider {
	
	@Override
	public void configure(int latency, int minEnergyReceived,
			int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived,
				minActivationEnergy, maxStoredEnergy);

		this.latency = 0;
	}

}
