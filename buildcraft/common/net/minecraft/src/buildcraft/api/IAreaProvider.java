/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

package net.minecraft.src.buildcraft.api;

/**
 * To be implemented by TileEntities able to provide a square area on the world,
 * typically BuildCraft markers. 
 */
public interface IAreaProvider {

	public int xMin ();
	public int yMin ();
	public int zMin ();
	
	public int xMax ();
	public int yMax ();
	public int zMax ();
	
	/**
	 * Remove from the world all objects used to define the area.
	 */
	public void removeFromWorld ();

}
