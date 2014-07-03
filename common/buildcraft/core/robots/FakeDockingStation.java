/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.DockingStationRegistry;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;

public class FakeDockingStation implements IDockingStation {

	private int x, y, z;
	private ForgeDirection side;

	public FakeDockingStation() {
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	@Override
	public int z() {
		return z;
	}

	@Override
	public ForgeDirection side() {
		return side;
	}

	@Override
	public EntityRobotBase reserved() {
		return null;
	}

	@Override
	public EntityRobotBase linked() {
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setInteger("side", side.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
		side = ForgeDirection.values()[nbt.getInteger("side")];
	}

	public DockingStation getRealStation (World world) {
		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);

		if (chunk != null) {
			return (DockingStation) DockingStationRegistry.getStation(x, y, z, side);
		} else {
			return null;
		}
	}
}

