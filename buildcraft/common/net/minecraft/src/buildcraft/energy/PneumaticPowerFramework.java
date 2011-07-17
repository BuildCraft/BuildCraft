package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.PowerFramework;

public class PneumaticPowerFramework extends PowerFramework {
	
	@Override
	public PowerProvider createPowerProvider() {
		return new PneumaticPowerProvider();
	}

}
