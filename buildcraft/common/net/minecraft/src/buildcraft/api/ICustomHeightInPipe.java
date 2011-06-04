package net.minecraft.src.buildcraft.api;

public interface ICustomHeightInPipe {

	/**
	 * Return the floor where to place objects in the pipe, typically 0.4 for
	 * regular blocks, and 0.27 for item sprites. 
	 */
	public float getHeightInPipe (); 
	
}
