/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
