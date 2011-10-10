/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.IBlockAccess;

public interface IBlockPipe {

	/**
	 * With special kind of pipes, connectors texture has to vary (e.g. 
	 * diamond or iron pipes. 
	 */
	public void prepareTextureFor (IBlockAccess blockAccess, int i, int j, int k, Orientations connection);
	
}
