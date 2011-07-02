package net.minecraft.src.buildcraft.core;

public interface IPowerReceptor {

	public int minEnergyExpected ();

	public int maxEnergyExpected ();
	
	public void receiveEnergy (int energy);
}
