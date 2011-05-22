package net.minecraft.src.buildcraft.core;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;


public interface IPipeEntry {
	
	public void entityEntering (EntityPassiveItem item, Orientations orientation);
	
	public Position getPosition ();
}
