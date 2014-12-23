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

	public AIRobotGoAndLinkToDock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGoAndLinkToDock(EntityRobotBase iRobot, DockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void start() {
		if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
			terminate();
		} else {
			if (station.takeAsMain(robot)) {
				startDelegateAI(new AIRobotGotoBlock(robot, station.pos().offset(station.side(), 2)));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotStraightMoveTo(robot,
					station.pos().getX() + 0.5F + station.side.getFrontOffsetX() * 0.5F,
					station.pos().getY() + 0.5F + station.side.getFrontOffsetY() * 0.5F,
					station.pos().getZ() + 0.5F + station.side.getFrontOffsetZ() * 0.5F));
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
			station.release(robot);
		}
	}
}
