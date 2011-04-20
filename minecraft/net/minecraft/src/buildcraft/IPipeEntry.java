package net.minecraft.src.buildcraft;

public interface IPipeEntry {
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation);
	
	public Position getPosition ();
}
