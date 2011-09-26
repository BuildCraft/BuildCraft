package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public abstract class TileMachine extends TileBuildCraft implements IMachine, IPowerReceptor {
	
	@Override
	public int powerRequest() {
		if (isActive()) {
			return getPowerProvider().maxEnergyReceived;
		} else {
			return 0;
		}		
	}
	
}
