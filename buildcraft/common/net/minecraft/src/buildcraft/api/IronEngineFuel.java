/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

package net.minecraft.src.buildcraft.api;

public class IronEngineFuel {
	public final int fuelId;
	public final int powerPerCycle;
	public final int totalBurningTime;
	
	public IronEngineFuel (int fuelId, int powerPerCycle, int totalBurningTime) {
		this.fuelId = fuelId;
		this.powerPerCycle = powerPerCycle;
		this.totalBurningTime = totalBurningTime;
	}
}
