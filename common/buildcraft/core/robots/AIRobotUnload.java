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

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.silicon.statements.ActionStationRequestItems;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class AIRobotUnload extends AIRobot {

	private int waitedCycles = 0;

	public AIRobotUnload(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		waitedCycles++;

		if (waitedCycles > 40) {
			if (!doUnload()) {
				terminate();
			} else {
				waitedCycles = 0;
			}
		}
	}

	private boolean doUnload() {
		DockingStation station = (DockingStation) robot.getDockingStation();

		if (station == null) {
			return false;
		}

		Pipe pipe = station.pipe.pipe;

		for (IInvSlot robotSlot : InventoryIterator.getIterable(robot, ForgeDirection.UNKNOWN)) {
			if (robotSlot.getStackInSlot() == null) {
				continue;
			}

			for (ActionSlot s : new ActionIterator(station.pipe.pipe)) {
				if (s.action instanceof ActionStationRequestItems) {
					if (((ActionStationRequestItems) s.action)
							.insert(station, (EntityRobot) robot, s, robotSlot, true)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public double getEnergyCost() {
		return 2;
	}
}
