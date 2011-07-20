package net.minecraft.src.buildcraft.api;

import net.minecraft.src.IBlockAccess;

public interface IPipeConnection {
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2);
}
