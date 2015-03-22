/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IPipe;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.statements.ActionStationInputItems;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.api.statements.StatementSlot;

public class AIRobotUnload extends AIRobot {

	private int waitedCycles = 0;
	private boolean delivered = false;

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

		IPipe pipe = station.getPipe().getPipe();

		for (IInvSlot robotSlot : InventoryIterator.getIterable(robot, ForgeDirection.UNKNOWN)) {
			if (robotSlot.getStackInSlot() == null) {
				continue;
			}

			for (StatementSlot s : new ActionIterator(pipe)) {
				if (s.statement instanceof ActionStationInputItems) {
					if (((ActionStationInputItems) s.statement)
							.insert(station, (EntityRobot) robot, s, robotSlot, true)) {

						delivered = true;

						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public int getEnergyCost() {
		return 10;
	}

	@Override
	public boolean success() {
		return delivered;
	}
}
