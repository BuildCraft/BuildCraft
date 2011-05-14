package net.minecraft.src.buildcraft.core;

import net.minecraft.src.IBlockAccess;

public interface IPipeConnection {
	public boolean isPipeConnected (IBlockAccess iBlockAccess, int x, int y, int z);
}
