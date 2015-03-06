/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.robots.DockingStation;
import buildcraft.robots.EntityRobot;
import buildcraft.robots.statements.ActionStationInputItems;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

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

		Pipe pipe = station.getPipe().pipe;

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
