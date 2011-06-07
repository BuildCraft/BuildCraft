package net.minecraft.src.buildcraft.api;

import net.minecraft.src.buildcraft.core.ICustomTextureBlock;

public interface IBlockPipe extends ICustomHeightInPipe, ICustomTextureBlock {

	/**
	 * With special kind of pipes, connectors texture has to vary (e.g. 
	 * diamond or iron pipes. 
	 */
	public int getTextureForConnection (Orientations connection, int metadata);
	
}
