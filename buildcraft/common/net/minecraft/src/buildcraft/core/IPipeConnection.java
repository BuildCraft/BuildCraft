package net.minecraft.src.buildcraft.core;

import net.minecraft.src.IBlockAccess;
import net.minecraft.src.World;

public interface IPipeConnection {
	public boolean isPipeConnected (IBlockAccess blockAccess, int x, int y, int z);
}
