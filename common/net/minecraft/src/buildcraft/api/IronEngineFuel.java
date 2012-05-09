/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
