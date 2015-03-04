/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.RobotUtils;

public class RobotsTriggerProvider implements ITriggerProvider {

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		LinkedList<ITriggerInternal> result = new LinkedList<ITriggerInternal>();
		TileEntity tile = container.getTile();

		if (!(tile instanceof IPipeTile)) {
			return result;
		}

		ArrayList<DockingStation> stations = new ArrayList<DockingStation>();

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (RobotUtils.getStation((IPipeTile) tile, dir) != null) {
				stations.add(RobotUtils.getStation((IPipeTile) tile, dir));
			}
		}

		if (stations.size() == 0) {
			return result;
		}

		result.add(BuildCraftRobotics.triggerRobotSleep);

		return result;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(ForgeDirection side, TileEntity tile) {
		return null;
	}

}
