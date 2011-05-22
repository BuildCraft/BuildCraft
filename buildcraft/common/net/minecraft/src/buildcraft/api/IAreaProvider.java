package net.minecraft.src.buildcraft.api;

public interface IAreaProvider {

	public int xMin ();
	public int yMin ();
	public int zMin ();
	
	public int xMax ();
	public int yMax ();
	public int zMax ();
	
	public void removeFromWorld ();

	
}
