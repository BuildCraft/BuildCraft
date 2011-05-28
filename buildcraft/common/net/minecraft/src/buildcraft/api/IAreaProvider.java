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
	
	public IBox getBox ();

	
}
