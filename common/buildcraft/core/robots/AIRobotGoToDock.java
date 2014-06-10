/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.robots.AIRobot;
import buildcraft.robots.DockingStation;
import buildcraft.robots.EntityRobotBase;

public class AIRobotGoToDock extends AIRobot {

	private DockingStation station;

	public AIRobotGoToDock(EntityRobotBase iRobot, DockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void start() {
		if (station == robot.getCurrentDockingStation()) {
			terminate();
		} else {
			station.reserved = robot;

			startDelegateAI(new AIRobotMoveToBlock(robot,
				station.pipe.xCoord + station.side.offsetX * 2,
				station.pipe.yCoord + station.side.offsetY * 2,
				station.pipe.zCoord + station.side.offsetZ * 2));
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
			robot.setCurrentDockingStation(station);
			station = null;
			terminate();
		}
	}

	@Override
	public void end() {
		// If there's still a station targeted, it was not reached. The AI has
		// probably been interrupted. Cancel reservation.
		if (station != null) {
			station.reserved = null;
		}
	}
}
