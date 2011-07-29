package net.minecraft.src.buildcraft.core;

import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;

public class RedstonePowerFramework extends PowerFramework {
	
	@Override
	public PowerProvider createPowerProvider() {
		return new RedstonePowerProvider();
	}

}
