/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.robots.AIRobot;
import buildcraft.robots.DockingStation;
import buildcraft.robots.DockingStationRegistry;
import buildcraft.robots.EntityRobotBase;
import buildcraft.transport.PipeTransportPower;

public class AIRobotRecharge extends AIRobot {

	private DockingStation axeDocking = null;

	public AIRobotRecharge(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		if (robot.getCurrentDockingStation() == null
				|| robot.getCurrentDockingStation().pipe.getPipeType() != PipeType.POWER) {

			for (DockingStation d : DockingStationRegistry.getStations()) {
				if (d.reserved != null) {
					continue;
				}

				if (d.pipe.getPipeType() == PipeType.POWER) {
					startDelegateAI(new AIRobotGoToDock(robot, d));
					break;
				}
			}
		} else {
			PipeTransportPower powerProvider = (PipeTransportPower) robot.getCurrentDockingStation().pipe.pipe.transport;

			powerProvider.requestEnergy(robot.getCurrentDockingStation().side, 100);
			robot.setEnergy(robot.getEnergy() + powerProvider.consumePower(robot.getCurrentDockingStation().side, 100));

			if (robot.getEnergy() >= EntityRobotBase.MAX_ENERGY) {
				terminate();
			}
		}
	}
}
