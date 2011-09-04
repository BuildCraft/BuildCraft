package net.minecraft.src.buildcraft.core;

public interface IMachine {

	public boolean isActive ();
	
	public boolean manageLiquids ();
	
	public boolean manageSolids ();
}
