package net.minecraft.src.buildcraft.api;

public interface IBlockPipe {

	/**
	 * With special kind of pipes, connectors texture has to vary (e.g. 
	 * diamond or iron pipes. 
	 */
	public int getTextureForConnection (Orientations connection, int metadata);
	
}
