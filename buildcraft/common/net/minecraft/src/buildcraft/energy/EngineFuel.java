/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

public class EngineFuel {
	public final int fuelId;
	public final int powerPerCycle;
	public final int totalBurningTime;
	
	public EngineFuel (int fuelId, int powerPerCycle, int totalBurningTime) {
		this.fuelId = fuelId;
		this.powerPerCycle = powerPerCycle;
		this.totalBurningTime = totalBurningTime;
	}
}
