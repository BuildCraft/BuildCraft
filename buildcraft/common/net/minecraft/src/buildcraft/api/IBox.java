package net.minecraft.src.buildcraft.api;

import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Position;


public interface IBox {

	public Position p1 ();
	
	public Position p2 ();
	
	public void createLasers (World world, LaserKind kind);
	
	public void deleteLasers ();
	
}
