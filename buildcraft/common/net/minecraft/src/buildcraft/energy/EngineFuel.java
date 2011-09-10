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
