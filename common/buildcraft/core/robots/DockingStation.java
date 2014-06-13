/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.transport.TileGenericPipe;

public class DockingStation implements IDockingStation {
	public TileGenericPipe pipe;
	public ForgeDirection side;
	public EntityRobotBase reserved;

	public DockingStation(TileGenericPipe iPipe, ForgeDirection iSide) {
		pipe = iPipe;
		side = iSide;
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
}

