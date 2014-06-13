/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStationRegistry;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.PipeTransportPower;

public class AIRobotRecharge extends AIRobot {

	private DockingStation axeDocking = null;

	public AIRobotRecharge(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void update() {
		if (robot.getCurrentDockingStation() == null
				|| ((DockingStation) robot.getCurrentDockingStation()).pipe.getPipeType() != PipeType.POWER) {

			for (IDockingStation d : DockingStationRegistry.getStations()) {
				DockingStation station = (DockingStation) d;

				if (station.reserved != null) {
					continue;
				}

				if (station.pipe.getPipeType() == PipeType.POWER) {
					startDelegateAI(new AIRobotGoToDock(robot, station));
					break;
				}
			}
		} else {
			PipeTransportPower powerProvider = (PipeTransportPower) ((DockingStation) robot.getCurrentDockingStation()).pipe.pipe.transport;

			powerProvider.requestEnergy(robot.getCurrentDockingStation().side(), 100);
			robot.setEnergy(robot.getEnergy()
					+ powerProvider.consumePower(robot.getCurrentDockingStation().side(), 100));

			if (robot.getEnergy() >= EntityRobotBase.MAX_ENERGY) {
				terminate();
			}
		}
	}
}
