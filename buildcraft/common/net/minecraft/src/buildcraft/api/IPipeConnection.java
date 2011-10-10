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

public interface IPipeConnection {
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2);
}
