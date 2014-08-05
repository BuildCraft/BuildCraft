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
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;

public class AIRobotGotoStation extends AIRobot {

	private IDockingStation station;

	public AIRobotGotoStation(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStation(EntityRobotBase iRobot, IDockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void start() {
		if (station == robot.getDockingStation()) {
			terminate();
		} else {
			if (robot.reserveStation(station)) {
				startDelegateAI(new AIRobotGotoBlock(robot,
						station.x() + station.side().offsetX * 2,
						station.y() + station.side().offsetY * 2,
						station.z() + station.side().offsetZ * 2));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void update() {
		// this should not happen, always terminate in this situation
		terminate();
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			if (station == null) {
				// in case of a load from disk
				station = robot.getReservedStation();
			}

			startDelegateAI(new AIRobotStraightMoveTo(robot,
					station.x() + 0.5F + station.side().offsetX * 0.5F,
					station.y() + 0.5F + station.side().offsetY * 0.5F,
					station.z() + 0.5F + station.side().offsetZ * 0.5F));
		} else {
			if (station == null) {
				// in case of a load from disk
				station = robot.getReservedStation();
			}

			robot.dock(station);
			station = null;
			terminate();
		}
	}

	@Override
	public void end() {
		// If there's still a station targeted, it was not reached. The AI has
		// probably been interrupted. Cancel reservation.
		if (station != null) {
			robot.reserveStation(null);
		}
	}
}
