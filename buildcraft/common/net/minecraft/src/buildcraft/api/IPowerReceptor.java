package net.minecraft.src.buildcraft.api;

public interface IPowerReceptor {
	
	public void setPowerProvider (PowerProvider provider);
	
	public PowerProvider getPowerProvider ();
	
	public void doWork ();
}
