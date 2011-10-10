/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

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
