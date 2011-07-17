package net.minecraft.src.buildcraft.core;

public interface IPowerReceptor {
	
	public void setPowerProvider (PowerProvider provider);
	
	public PowerProvider getPowerProvider ();
	
	public void doWork ();
}
