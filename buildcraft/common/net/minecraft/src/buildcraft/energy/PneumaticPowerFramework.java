package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;

public class PneumaticPowerFramework extends PowerFramework {
	
	@Override
	public PowerProvider createPowerProvider() {
		return new PneumaticPowerProvider();
	}

}
