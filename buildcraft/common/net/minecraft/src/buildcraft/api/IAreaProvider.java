/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
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
