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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.transport.TileGenericPipe;

public class DockingStation implements IDockingStation {
	public TileGenericPipe pipe;
	public ForgeDirection side;
	private EntityRobotBase linked;
	private EntityRobotBase reserved;

	public DockingStation(TileGenericPipe iPipe, ForgeDirection iSide) {
		pipe = iPipe;
		side = iSide;
	}

	public DockingStation() {
	}

	@Override
	public int x() {
		return pipe.xCoord;
	}

	@Override
	public int y() {
		return pipe.yCoord;
	}

	@Override
	public int z() {
		return pipe.zCoord;
	}

	@Override
	public ForgeDirection side() {
		return side;
	}

	@Override
	public EntityRobotBase reserved() {
		return reserved;
	}

	@Override
	public EntityRobotBase linked() {
		return linked;
	}

	public boolean reserve(EntityRobotBase robot) {
		if ((linked == null || linked == robot) && reserved == null) {
			reserved = robot;
			pipe.scheduleRenderUpdate();
			return true;
		} else {
			return false;
		}
	}

	public boolean link(EntityRobotBase robot) {
		if (linked == null) {
			linked = robot;
			pipe.scheduleRenderUpdate();
			return true;
		} else {
			return false;
		}
	}

	public void unreserve(EntityRobotBase robot) {
		if (reserved == robot) {
			reserved = null;
			pipe.scheduleRenderUpdate();
		}
	}

	public void unlink(EntityRobotBase robot) {
		if (linked == robot) {
			linked = null;
			pipe.scheduleRenderUpdate();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("x", pipe.xCoord);
		nbt.setInteger("y", pipe.yCoord);
		nbt.setInteger("z", pipe.zCoord);
		nbt.setInteger("side", side.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}
}

