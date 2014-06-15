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

public class AIRobotGoAndLinkToDock extends AIRobot {

	public DockingStation station;

	public AIRobotGoAndLinkToDock(EntityRobotBase iRobot, DockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void start() {
		if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
			terminate();
		} else {
			if (robot.linkToStation(station)) {
				startDelegateAI(new AIRobotMoveToBlock(robot,
						station.pipe.xCoord + station.side.offsetX * 2,
						station.pipe.yCoord + station.side.offsetY * 2,
						station.pipe.zCoord + station.side.offsetZ * 2));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotMoveToBlock) {
			startDelegateAI(new AIRobotStraightMoveTo(robot,
					station.pipe.xCoord + 0.5F + station.side.offsetX * 0.5F,
					station.pipe.yCoord + 0.5F + station.side.offsetY * 0.5F,
					station.pipe.zCoord + 0.5F + station.side.offsetZ * 0.5F));
		} else {
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
