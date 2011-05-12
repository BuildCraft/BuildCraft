package net.minecraft.src.buildcraft.core;

public interface IPipeEntry extends IPipeConnection {
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation);
	
	public Position getPosition ();
}
