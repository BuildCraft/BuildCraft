/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;

public class RobotsTriggerProvider implements ITriggerProvider {
	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		LinkedList<ITriggerInternal> result = new LinkedList<ITriggerInternal>();
		DockingStationIterator iterator = new DockingStationIterator(container);

		if (iterator.hasNext()) {
			result.add(BuildCraftRobotics.triggerRobotSleep);
			result.add(BuildCraftRobotics.triggerRobotInStation);
			result.add(BuildCraftRobotics.triggerRobotLinked);
			result.add(BuildCraftRobotics.triggerRobotReserved);
		}

		return result;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(ForgeDirection side, TileEntity tile) {
		return null;
	}
}
