/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.api.power.PowerProvider;

public class PneumaticPowerProvider extends PowerProvider {

	@Override
	public void configure(int latency, int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy, int maxStoredEnergy) {
		super.configure(latency, minEnergyReceived, maxEnergyReceived, minActivationEnergy, maxStoredEnergy);

		this.latency = 0;
	}

}
