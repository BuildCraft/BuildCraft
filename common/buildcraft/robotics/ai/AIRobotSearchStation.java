/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionStationForbidRobot;

public class AIRobotSearchStation extends AIRobot {

	public DockingStation targetStation;
	private IStationFilter filter;
	private IZone zone;

	public AIRobotSearchStation(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchStation(EntityRobotBase iRobot, IStationFilter iFilter, IZone iZone) {
		super(iRobot);

		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void start() {
		if (robot.getDockingStation() != null
				&& filter.matches((DockingStation) robot.getDockingStation())) {
			targetStation = (DockingStation) robot.getDockingStation();
			terminate();
			return;
		}

		double potentialStationDistance = Float.MAX_VALUE;
		DockingStation potentialStation = null;

		for (DockingStation d : robot.getRegistry().getStations()) {
			DockingStation station = (DockingStation) d;

			if (d.isTaken() && d.robotIdTaking() != robot.getRobotId()) {
				continue;
			}

			if (zone != null && !zone.contains(d.x(), d.y(), d.z())) {
				continue;
			}

			if (filter.matches(station)) {
				if (ActionStationForbidRobot.isForbidden(station, robot)) {
					continue;
				}

				double dx = robot.posX - d.x();
				double dy = robot.posY - d.y();
				double dz = robot.posZ - d.z();
				double distance = dx * dx + dy * dy + dz * dz;

				if (potentialStation == null || distance < potentialStationDistance) {
					potentialStation = station;
					potentialStationDistance = distance;
				}
			}
		}

		if (potentialStation != null) {
			targetStation = potentialStation;
		}

		terminate();
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		terminate();
	}

	@Override
	public boolean success () {
		return targetStation != null;
	}
}
