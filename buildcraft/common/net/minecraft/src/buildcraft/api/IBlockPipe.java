package net.minecraft.src.buildcraft.api;

import net.minecraft.src.IBlockAccess;

public interface IBlockPipe {

	/**
	 * With special kind of pipes, connectors texture has to vary (e.g. 
	 * diamond or iron pipes. 
	 */
	public void prepareTextureFor (IBlockAccess blockAccess, int i, int j, int k, Orientations connection);
	
}
