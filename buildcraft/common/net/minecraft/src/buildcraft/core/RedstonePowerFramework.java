package net.minecraft.src.buildcraft.core;

public class RedstonePowerFramework extends PowerFramework {
	
	@Override
	public PowerProvider createPowerProvider() {
		return new RedstonePowerProvider();
	}

}
